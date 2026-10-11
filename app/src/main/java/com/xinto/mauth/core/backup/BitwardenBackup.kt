package com.xinto.mauth.core.backup

import com.jsoizo.kotlincsv.csvReader
import com.xinto.mauth.core.otp.OtpUriParserResult
import com.xinto.mauth.core.otp.parseOtpUri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.io.encoding.Base64

object BitwardenBackup : BackupImporter {

    private const val KDF_PBKDF2 = 0
    private const val KDF_ARGON2ID = 1
    private const val KEY_LENGTH = 32
    private const val ENC_TYPE_AES_CBC_HMAC = "2"
    private val INFO_ENC = "enc".toByteArray(Charsets.UTF_8)
    private val INFO_MAC = "mac".toByteArray(Charsets.UTF_8)

    private const val COLUMN_TOTP = "login_totp"
    private const val COLUMN_NAME = "name"
    private const val COLUMN_USERNAME = "login_username"

    private val json = Json { ignoreUnknownKeys = true }

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()
        if (trimmed.startsWith("{"))
            return trimmed.contains("\"items\"") && trimmed.contains("\"login\"")

        return trimmed.lineSequence().first().contains(COLUMN_TOTP)
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        if (content.trimStart().startsWith("{"))
            return parseJson(content, password)

        return parseCsv(content)
    }

    private fun parseCsv(content: String): ImportResult {
        val rows = csvReader().readAll(content)
        if (rows.isEmpty())
            return ImportResult.Failure(ImportFailure.Unrecognized)

        val header = rows.first().map { it.trim().lowercase() }
        val totpIndex = header.indexOf(COLUMN_TOTP)
        if (totpIndex < 0)
            return ImportResult.Failure(ImportFailure.Unrecognized)

        val nameIndex = header.indexOf(COLUMN_NAME)
        val usernameIndex = header.indexOf(COLUMN_USERNAME)
        val entries = rows.drop(1).map { row ->
            BitwardenEntry(
                totp = row.getOrNull(totpIndex),
                name = row.getOrNull(nameIndex).orEmpty(),
                username = row.getOrNull(usernameIndex).orEmpty()
            )
        }

        return collect(entries)
    }

    private fun parseJson(content: String, password: CharArray?): ImportResult {
        val header = json.decodeFromString<BitwardenHeader>(content)
        val decrypted = if (header.encrypted == true) {
            if (header.passwordProtected != true)
                return ImportResult.Failure(ImportFailure.Unrecognized)

            if (password == null)
                return ImportResult.NeedsPassword

            decrypt(header, password)
                ?: return ImportResult.Failure(ImportFailure.WrongPassword)
        } else {
            content
        }

        val export = json.decodeFromString<BitwardenExport>(decrypted)
        val entries = export.items.map { item ->
            BitwardenEntry(
                totp = item.login?.totp,
                name = item.name.orEmpty(),
                username = item.login?.username.orEmpty()
            )
        }

        return collect(entries)
    }

    private fun decrypt(header: BitwardenHeader, password: CharArray): String? {
        val salt = header.salt?.toByteArray(Charsets.UTF_8) ?: return null
        val data = header.data ?: return null
        val iterations = header.kdfIterations ?: return null

        // Reads Bitwarden's EncString: `2.[iv]|[data]|[mac]`, each part Base64.
        val type = data.substringBefore('.', missingDelimiterValue = "")
        if (type != ENC_TYPE_AES_CBC_HMAC)
            return null

        val parts = data.substringAfter('.').split('|')
        if (parts.size != 3)
            return null

        val material = when (header.kdfType) {
            KDF_PBKDF2 -> {
                BackupCrypto.derivePbkdf2(
                    password = password,
                    salt = salt,
                    iterations = iterations,
                    hmac = BackupCrypto.Pbkdf2Hmac.SHA256
                )
            }

            KDF_ARGON2ID -> {
                val memory = header.kdfMemory ?: return null
                val parallelism = header.kdfParallelism ?: return null

                BackupCrypto.deriveArgon2id(
                    password = password,
                    salt = BackupCrypto.sha256(salt),
                    memoryKib = memory * 1024,
                    iterations = iterations,
                    parallelism = parallelism
                )
            }

            else -> return null
        }

        val encKey = BackupCrypto.hkdfExpand(material, INFO_ENC, KEY_LENGTH)
        val macKey = BackupCrypto.hkdfExpand(material, INFO_MAC, KEY_LENGTH)
        try {
            return BackupCrypto.decryptCbcHmac(
                iv = Base64.Mime.decode(parts[0]),
                ciphertext = Base64.Mime.decode(parts[1]),
                mac = Base64.Mime.decode(parts[2]),
                encKey = encKey,
                macKey = macKey
            )?.toString(Charsets.UTF_8)
        } finally {
            material.fill(0)
            encKey.fill(0)
            macKey.fill(0)
        }
    }

    private fun collect(entries: List<BitwardenEntry>): ImportResult {
        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (entry in entries) {
            val totp = entry.totp?.trim()
            if (totp.isNullOrEmpty())
                continue

            if (totp.startsWith("otpauth", ignoreCase = true)) {
                when (val result = totp.parseOtpUri()) {
                    is OtpUriParserResult.Success -> {
                        accounts.add(result.data.toBackupAccount())
                    }
                    is OtpUriParserResult.Multipart -> {
                        result.data.forEach {
                            accounts.add(it.toBackupAccount())
                        }
                    }
                    is OtpUriParserResult.Failure -> {
                        rejected.add(RejectedEntry(entry.name, "", ImportFailure.Malformed))
                    }
                }
            } else {
                accounts.add(
                    BackupAccount(
                        id = UUID.randomUUID().toString(),
                        name = entry.username.ifEmpty { entry.name },
                        issuer = entry.name,
                        secret = totp,
                        type = BackupOtpType.TOTP,
                        period = 30
                    )
                )
            }
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(ImportFailure.Unrecognized)

        return ImportResult.Success(accounts, rejected = rejected)
    }

    private data class BitwardenEntry(
        val totp: String?,
        val name: String,
        val username: String
    )

    @Serializable
    private data class BitwardenHeader(
        val encrypted: Boolean? = null,
        val passwordProtected: Boolean? = null,
        val salt: String? = null,
        val kdfType: Int? = null,
        val kdfIterations: Int? = null,
        val kdfMemory: Int? = null,
        val kdfParallelism: Int? = null,
        val data: String? = null
    )

    @Serializable
    private data class BitwardenExport(
        val encrypted: Boolean? = null,
        val items: List<BitwardenItem> = emptyList()
    )

    @Serializable
    private data class BitwardenItem(
        val name: String? = null,
        val login: BitwardenLogin? = null
    )

    @Serializable
    private data class BitwardenLogin(
        val username: String? = null,
        val totp: String? = null
    )
}
