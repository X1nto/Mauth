package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.core.backup.model.toBackupAccount
import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.otp.OtpUriParserResult
import com.xinto.mauth.core.otp.parseOtpUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.crypto.AEADBadTagException
import kotlin.io.encoding.Base64

class ProtonImporter(
    private val crypto: BackupCrypto
) : AccountImporter {

    override val source = ImportSource.Proton

    private val json = Json { ignoreUnknownKeys = true }

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()

        if (!trimmed.startsWith("{"))
            return false

        if (!trimmed.contains("\"version\""))
            return false

        return trimmed.contains("\"entry_type\"") ||
                (trimmed.contains("\"salt\"") && trimmed.contains("\"content\""))
    }

    override fun parse(content: String, password: CharArray?): ImportResult {
        val header = json.decodeFromString<ProtonHeader>(content)
        if (header.version != SUPPORTED_VERSION)
            return ImportResult.Failure(source, ImportFailure.UnsupportedVersion)

        val plaintext = if (header.salt != null && header.content != null) {
            if (password == null)
                return ImportResult.NeedsPassword(source)

            decrypt(header.salt, header.content, password)
                ?: return ImportResult.Failure(source, ImportFailure.WrongPassword)
        } else {
            content
        }

        val export = json.decodeFromString<ProtonExport>(plaintext)
        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (entry in export.entries) {
            val uri = entry.content.uri.trim()
            val name = entry.content.name.orEmpty()

            if (!uri.startsWith("otpauth", ignoreCase = true)) {
                rejected.add(RejectedEntry(name, "", ImportFailure.Unrecognized))
                continue
            }

            when (val result = uri.parseOtpUri()) {
                is OtpUriParserResult.Success -> {
                    accounts.add(result.data.toBackupAccount())
                }
                is OtpUriParserResult.Multipart -> {
                    result.data.forEach {
                        accounts.add(it.toBackupAccount())
                    }
                }
                is OtpUriParserResult.Failure -> {
                    rejected.add(RejectedEntry(name, "", ImportFailure.Malformed))
                }
            }
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(source, ImportFailure.Unrecognized)

        return ImportResult.Success(source = source, accounts = accounts, rejected = rejected)
    }

    private fun decrypt(salt: String, content: String, password: CharArray): String? {
        val saltBytes = Base64.decode(salt)
        val payload = Base64.decode(content)
        if (payload.size <= NONCE_LENGTH)
            return null

        val key = crypto.deriveArgon2id(
            password = password,
            salt = saltBytes,
            memoryKib = ARGON2_MEMORY_KIB,
            iterations = ARGON2_ITERATIONS,
            parallelism = ARGON2_PARALLELISM
        )

        return try {
            crypto.decryptRaw(
                ciphertext = payload.copyOfRange(NONCE_LENGTH, payload.size),
                nonce = payload.copyOfRange(0, NONCE_LENGTH),
                key = key,
                aad = AAD
            ).toString(Charsets.UTF_8)
        } catch (e: AEADBadTagException) {
            null
        } finally {
            key.fill(0)
        }
    }

    @Serializable
    private data class ProtonHeader(
        val version: Int = 0,
        val salt: String? = null,
        val content: String? = null
    )

    @Serializable
    private data class ProtonExport(
        val version: Int = 0,
        val entries: List<ProtonEntry> = emptyList()
    )

    @Serializable
    private data class ProtonEntry(
        val id: String = "",
        val content: ProtonEntryContent,
        val note: String? = null
    )

    @Serializable
    private data class ProtonEntryContent(
        val uri: String,
        @SerialName("entry_type") val entryType: String? = null,
        val name: String? = null
    )

    private companion object {
        const val SUPPORTED_VERSION = 1
        const val NONCE_LENGTH = 12

        const val ARGON2_MEMORY_KIB = 19 * 1024
        const val ARGON2_ITERATIONS = 2
        const val ARGON2_PARALLELISM = 1

        val AAD = "proton.authenticator.export.v1".toByteArray(Charsets.UTF_8)
    }
}
