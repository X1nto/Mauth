package com.xinto.mauth.core.backup

import com.xinto.mauth.core.otp.OtpUriParserResult
import com.xinto.mauth.core.otp.parseOtpUri
import com.xinto.mauth.core.otp.toOtpUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.crypto.AEADBadTagException
import kotlin.io.encoding.Base64

object ProtonBackup : BackupImporter, BackupExporter {

    private const val SUPPORTED_VERSION = 1
    private const val NONCE_LENGTH = 12
    private const val SALT_LENGTH = 16
    private const val ENTRY_TYPE_TOTP = "Totp"

    private val SUPPORTED_DIGITS = 1..9

    private const val ARGON2_MEMORY_KIB = 19 * 1024
    private const val ARGON2_ITERATIONS = 2
    private const val ARGON2_PARALLELISM = 1

    private val AAD = "proton.authenticator.export.v1".toByteArray(Charsets.UTF_8)

    private val json = Json { ignoreUnknownKeys = true }

    override val mimeType = MimeType.Json

    override val supportsEncryption = true

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()

        if (!trimmed.startsWith("{"))
            return false

        if (!trimmed.contains("\"version\""))
            return false

        return trimmed.contains("\"entry_type\"") ||
                (trimmed.contains("\"salt\"") && trimmed.contains("\"content\""))
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val header = json.decodeFromString<ProtonHeader>(content)
        if (header.version != SUPPORTED_VERSION)
            return ImportResult.Failure(ImportFailure.UnsupportedVersion)

        val plaintext = if (header.salt != null && header.content != null) {
            if (password == null)
                return ImportResult.NeedsPassword

            decrypt(header.salt, header.content, password)
                ?: return ImportResult.Failure(ImportFailure.WrongPassword)
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
                is OtpUriParserResult.Failure -> {
                    rejected.add(RejectedEntry(name, "", ImportFailure.Malformed))
                }
            }
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(ImportFailure.Unrecognized)

        return ImportResult.Success(accounts = accounts, rejected = rejected)
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        // Proton doesn't support HOTP
        val entries = contents.accounts
            .filter { it.type == BackupOtpType.TOTP && it.digits in SUPPORTED_DIGITS }
            .map {
                ProtonEntry(
                    id = it.id,
                    content = ProtonEntryContent(
                        uri = it.toOtpData().toOtpUri(),
                        entryType = ENTRY_TYPE_TOTP,
                        name = it.name.ifEmpty { it.issuer }
                    )
                )
            }

        val plaintext = json.encodeToString(ProtonExport(version = SUPPORTED_VERSION, entries = entries))
        if (password == null)
            return plaintext

        val salt = BackupCrypto.randomBytes(SALT_LENGTH)
        val nonce = BackupCrypto.randomBytes(NONCE_LENGTH)
        val key = BackupCrypto.deriveArgon2id(
            password = password,
            salt = salt,
            memoryKib = ARGON2_MEMORY_KIB,
            iterations = ARGON2_ITERATIONS,
            parallelism = ARGON2_PARALLELISM
        )

        val ciphertext = try {
            BackupCrypto.encryptRaw(
                plaintext = plaintext.toByteArray(Charsets.UTF_8),
                nonce = nonce,
                key = key,
                aad = AAD
            )
        } finally {
            key.fill(0)
        }

        return json.encodeToString(
            ProtonHeader(
                version = SUPPORTED_VERSION,
                salt = Base64.encode(salt),
                content = Base64.encode(nonce + ciphertext)
            )
        )
    }

    private fun decrypt(salt: String, content: String, password: CharArray): String? {
        val saltBytes = Base64.decode(salt)
        val payload = Base64.decode(content)
        if (payload.size <= NONCE_LENGTH)
            return null

        val key = BackupCrypto.deriveArgon2id(
            password = password,
            salt = saltBytes,
            memoryKib = ARGON2_MEMORY_KIB,
            iterations = ARGON2_ITERATIONS,
            parallelism = ARGON2_PARALLELISM
        )

        return try {
            BackupCrypto.decryptRaw(
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
}
