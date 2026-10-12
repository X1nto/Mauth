package com.xinto.mauth.core.backup

import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.util.UUID
import javax.crypto.AEADBadTagException

object AndOtpBackup : BackupImporter {

    private const val SALT_LENGTH = 12
    private const val NONCE_LENGTH = 12
    private const val TAG_LENGTH = 16
    private const val HEADER_LENGTH = Int.SIZE_BYTES + SALT_LENGTH + NONCE_LENGTH

    private const val MIN_ITERATIONS = 1_000
    private const val MAX_ITERATIONS = 10_000_000

    private val json = Json { ignoreUnknownKeys = true }

    override fun sniff(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.startsWith("[")) {
            return trimmed.contains("\"secret\"") &&
                (trimmed.contains("\"used_frequency\"") || trimmed.contains("\"thumbnail\""))
        }

        return isEncryptedPayload(trimmed)
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val trimmed = content.trim()

        val entries = if (trimmed.startsWith("[")) {
            json.decodeFromString<List<AndOtpEntry>>(trimmed)
        } else {
            if (password == null)
                return ImportResult.NeedsPassword

            val decrypted = decrypt(trimmed, password)
                ?: return ImportResult.Failure(ImportFailure.WrongPassword)

            try {
                json.decodeFromString<List<AndOtpEntry>>(decrypted.toString(Charsets.UTF_8))
            } finally {
                decrypted.fill(0)
            }
        }

        val tags = linkedSetOf<String>()

        val accounts = entries.map { entry ->
            val type = BackupOtpType.fromName(entry.type)

            val tag = entry.tags.firstOrNull { it.isNotBlank() }
            if (tag != null) {
                tags.add(tag)
            }

            BackupAccount(
                id = UUID.randomUUID().toString(),
                name = entry.label.orEmpty(),
                issuer = entry.issuer.orEmpty(),
                secret = entry.secret,
                type = type,
                algorithm = BackupOtpAlgorithm.fromName(entry.algorithm),
                digits = entry.digits ?: 6,
                period = if (type == BackupOtpType.TOTP) entry.period ?: 30 else null,
                counter = if (type == BackupOtpType.HOTP) entry.counter ?: 0 else null,
                group = tag
            )
        }

        return ImportResult.Success(
            accounts = accounts,
            groups = tags.map {
                BackupGroup(
                    id = it,
                    name = it
                )
            }
        )
    }

    private fun isEncryptedPayload(content: String): Boolean {
        val bytes = try {
            Base64.Mime.decode(content)
        } catch (e: Exception) {
            return false
        }

        if (bytes.size <= HEADER_LENGTH + TAG_LENGTH) return false
        val iterations = ByteBuffer.wrap(bytes, 0, Int.SIZE_BYTES).int
        return iterations in MIN_ITERATIONS..MAX_ITERATIONS
    }

    private fun decrypt(content: String, password: CharArray): ByteArray? {
        val bytes = Base64.Mime.decode(content)
        val iterations = ByteBuffer.wrap(bytes, 0, Int.SIZE_BYTES).int
        val salt = bytes.copyOfRange(Int.SIZE_BYTES, Int.SIZE_BYTES + SALT_LENGTH)
        val nonce = bytes.copyOfRange(HEADER_LENGTH - NONCE_LENGTH, HEADER_LENGTH)
        val ciphertext = bytes.copyOfRange(HEADER_LENGTH, bytes.size)

        val key = BackupCrypto.derivePbkdf2(
            password = password,
            salt = salt,
            iterations = iterations,
            hmac = BackupCrypto.Pbkdf2Hmac.SHA1
        )

        return try {
            BackupCrypto.decryptRaw(ciphertext, nonce, key)
        } catch (e: AEADBadTagException) {
            null
        } finally {
            key.fill(0)
        }
    }

    @Serializable
    private data class AndOtpEntry(
        val secret: String,
        val issuer: String? = null,
        val label: String? = null,
        val digits: Int? = null,
        val type: String? = null,
        val algorithm: String? = null,
        val period: Int? = null,
        val counter: Int? = null,
        val tags: List<String> = emptyList()
    )
}
