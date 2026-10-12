package com.xinto.mauth.core.backup

import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.crypto.AEADBadTagException

object TwoFasBackup : BackupImporter, BackupExporter {

    private const val PBKDF2_ITERATIONS = 10_000
    private const val SALT_LENGTH = 256
    private const val NONCE_LENGTH = 12

    private const val SCHEMA_VERSION = 4
    private const val APP_ORIGIN = "android"
    private const val SOURCE_MANUAL = "manual"
    private const val ICON_LABEL = "Label"
    private const val ICON_LABEL_COLOR = "Default"
    private const val ICON_COLLECTION_DEFAULT = "a5b3fb65-4ec5-43e6-8ec1-49e24ca9e7ad"

    private const val REFERENCE = "tRViSsLKzd86Hprh4ceC2OP7xazn4rrt4xhfEUbOjxLX8Rc3mkISXE0lWbmnWfggogbBJhtYgpK6fMl1D6mtsy92R3HkdGfwuXbzLebqVFJsR7IZ2w58t938iymwG4824igYy1wi6n2WDpO1Q1P69zwJGs2F5a1qP4MyIiDSD7NCV2OvidXQCBnDlGfmz0f1BQySRkkt4ryiJeCjD2o4QsveJ9uDBUn8ELyOrESv5R5DMDkD4iAF8TXU7KyoJujd"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    override val mimeType = MimeType.Json

    override val supportsEncryption = true

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

        val groups = backup.groups.orEmpty().map { BackupGroup(id = it.id, name = it.name) }
        val groupIds = groups.map { it.id }.toSet()

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
                    counter = if (type == BackupOtpType.HOTP) otp?.counter ?: 0 else null,
                    group = service.groupId?.takeIf { it in groupIds }
                )
            )
        }

        return ImportResult.Success(accounts = accounts, groups = groups, rejected = rejected)
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        val now = System.currentTimeMillis()
        val services = contents.accounts.mapIndexed { index, account ->
            account.toService(position = index, updatedAt = now)
        }
        val groups = contents.groups.map {
            TwoFasGroup(
                id = it.id,
                name = it.name,
                updatedAt = now
            )
        }

        val backup = TwoFasBackupFile(
            schemaVersion = SCHEMA_VERSION,
            appVersionCode = 0,
            appVersionName = "",
            appOrigin = APP_ORIGIN,
            updatedAt = now,
            groups = groups
        )

        if (password == null)
            return json.encodeToString(backup.copy(services = services))

        val salt = BackupCrypto.randomBytes(SALT_LENGTH)
        val key = BackupCrypto.derivePbkdf2(
            password = password,
            salt = salt,
            iterations = PBKDF2_ITERATIONS,
            hmac = BackupCrypto.Pbkdf2Hmac.SHA256
        )

        try {
            return json.encodeToString(
                backup.copy(
                    services = emptyList(),
                    servicesEncrypted = encryptPiece(json.encodeToString(services), salt, key),
                    reference = encryptPiece(REFERENCE, salt, key)
                )
            )
        } finally {
            key.fill(0)
        }
    }

    private fun encryptPiece(plaintext: String, salt: ByteArray, key: ByteArray): String {
        val nonce = BackupCrypto.randomBytes(NONCE_LENGTH)
        val ciphertext = BackupCrypto.encryptRaw(
            plaintext = plaintext.toByteArray(Charsets.UTF_8),
            nonce = nonce,
            key = key
        )
        return listOf(ciphertext, salt, nonce).joinToString(":") { Base64.encode(it) }
    }

    private fun BackupAccount.toService(position: Int, updatedAt: Long): TwoFasService {
        val serviceName = issuer.ifBlank { name }

        return TwoFasService(
            name = serviceName,
            secret = secret,
            updatedAt = updatedAt,
            otp = TwoFasOtp(
                account = name,
                issuer = issuer,
                digits = digits,
                period = period,
                counter = counter,
                algorithm = algorithm.name,
                tokenType = type.name,
                source = SOURCE_MANUAL
            ),
            order = TwoFasOrder(position = position),
            icon = TwoFasIcon(
                selected = ICON_LABEL,
                label = TwoFasIconLabel(
                    text = serviceName.take(2).uppercase(),
                    backgroundColor = ICON_LABEL_COLOR
                ),
                iconCollection = TwoFasIconCollection(id = ICON_COLLECTION_DEFAULT)
            ),
            groupId = group
        )
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
        val appVersionCode: Int? = null,
        val appVersionName: String? = null,
        val appOrigin: String? = null,
        val updatedAt: Long? = null,
        val services: List<TwoFasService>? = null,
        val groups: List<TwoFasGroup>? = null,
        val servicesEncrypted: String? = null,
        val reference: String? = null
    )

    @Serializable
    private data class TwoFasService(
        val name: String? = null,
        val secret: String? = null,
        val updatedAt: Long? = null,
        val otp: TwoFasOtp? = null,
        val order: TwoFasOrder? = null,
        val icon: TwoFasIcon? = null,
        val groupId: String? = null
    )

    @Serializable
    private data class TwoFasOrder(
        val position: Int
    )

    @Serializable
    private data class TwoFasIcon(
        val selected: String,
        val label: TwoFasIconLabel? = null,
        val iconCollection: TwoFasIconCollection
    )

    @Serializable
    private data class TwoFasIconLabel(
        val text: String,
        val backgroundColor: String
    )

    @Serializable
    private data class TwoFasIconCollection(
        val id: String
    )

    @Serializable
    private data class TwoFasGroup(
        val id: String,
        val name: String,
        val isExpanded: Boolean = true,
        val updatedAt: Long? = null
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
        val tokenType: String? = null,
        val source: String? = null
    )
}
