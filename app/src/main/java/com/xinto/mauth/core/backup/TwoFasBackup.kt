package com.xinto.mauth.core.backup

import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.crypto.AEADBadTagException

object TwoFasBackup : BackupImporter {

    private const val PBKDF2_ITERATIONS = 10_000

    private val json = Json { ignoreUnknownKeys = true }

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()
        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"services\"") || trimmed.contains("\"servicesEncrypted\"")
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val backup = json.decodeFromString<TwoFasBackupFile>(content)

        val services = when {
            !backup.services.isNullOrEmpty() -> backup.services
            backup.servicesEncrypted != null -> {
                if (password == null)
                    return ImportResult.NeedsPassword

                val decrypted = decryptServices(backup.servicesEncrypted, password)
                    ?: return ImportResult.Failure(ImportFailure.WrongPassword)

                try {
                    json.decodeFromString<List<TwoFasService>>(decrypted.toString(Charsets.UTF_8))
                } finally {
                    decrypted.fill(0)
                }
            }
            else -> emptyList()
        }

        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (service in services) {
            val secret = service.secret
            if (secret.isNullOrBlank()) {
                rejected.add(
                    RejectedEntry(
                        name = service.otp?.account ?: service.name.orEmpty(),
                        issuer = service.otp?.issuer ?: service.name.orEmpty(),
                        reason = ImportFailure.InvalidSecret
                    )
                )
                continue
            }

            val otp = service.otp
            val type = BackupOtpType.fromName(otp?.tokenType)
            accounts.add(
                BackupAccount(
                    id = UUID.randomUUID().toString(),
                    name = otp?.account ?: otp?.label ?: service.name.orEmpty(),
                    issuer = otp?.issuer ?: service.name.orEmpty(),
                    secret = secret,
                    type = type,
                    algorithm = BackupOtpAlgorithm.fromName(otp?.algorithm),
                    digits = otp?.digits ?: 6,
                    period = if (type == BackupOtpType.TOTP) otp?.period ?: 30 else null,
                    counter = if (type == BackupOtpType.HOTP) otp?.counter ?: 0 else null
                )
            )
        }

        return ImportResult.Success(accounts = accounts, rejected = rejected)
    }

    private fun decryptServices(payload: String, password: CharArray): ByteArray? {
        val parts = payload.split(":")
        if (parts.size < 3)
            return null

        val ciphertext = Base64.Mime.decode(parts[0])
        val salt = Base64.Mime.decode(parts[1])
        val nonce = Base64.Mime.decode(parts[2])
        val key = BackupCrypto.derivePbkdf2(
            password = password,
            salt = salt,
            iterations = PBKDF2_ITERATIONS,
            hmac = BackupCrypto.Pbkdf2Hmac.SHA256
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
    private data class TwoFasBackupFile(
        val schemaVersion: Int = 0,
        val services: List<TwoFasService>? = null,
        val servicesEncrypted: String? = null
    )

    @Serializable
    private data class TwoFasService(
        val name: String? = null,
        val secret: String? = null,
        val otp: TwoFasOtp? = null
    )

    @Serializable
    private data class TwoFasOtp(
        val label: String? = null,
        val account: String? = null,
        val issuer: String? = null,
        val digits: Int? = null,
        val period: Int? = null,
        val counter: Int? = null,
        val algorithm: String? = null,
        val tokenType: String? = null
    )
}
