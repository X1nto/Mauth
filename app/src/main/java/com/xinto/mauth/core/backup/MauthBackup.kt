package com.xinto.mauth.core.backup

import com.xinto.mauth.core.backup.ImportResult.Failure
import com.xinto.mauth.core.backup.ImportResult.Success
import com.xinto.mauth.ui.util.Base64Bytes
import com.xinto.mauth.ui.util.encodeToByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object MauthBackup : BackupImporter, BackupExporter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private const val VERSION = 1

    override val mimeType = MimeType.Json

    override val supportsEncryption = true

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()

        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"accounts\"") || trimmed.contains("\"encryption\"")
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val payload = try {
            json.decodeFromString<MauthPayload>(content)
        } catch (e: Exception) {
            return Failure(ImportFailure.Unrecognized)
        }

        if (payload.version > VERSION)
            return Failure(ImportFailure.UnsupportedVersion)

        val encryption = payload.encryption
        if (encryption == null) {
            if (payload.accounts == null)
                return Failure(ImportFailure.Unrecognized)

            return Success(
                accounts = payload.accounts.map { it.toDomain() },
                groups = payload.groups.orEmpty().map { it.toDomain() }
            )
        }

        if (password == null)
            return ImportResult.NeedsPassword

        if (encryption.kdf != MauthEncryption.KDF_ARGON2ID)
            return Failure(ImportFailure.UnsupportedVersion)

        val ciphertext = payload.data
            ?: return Failure(ImportFailure.Malformed)

        val plaintext = try {
            val params = Argon2Params(
                memoryKib = encryption.m,
                iterations = encryption.t,
                parallelism = encryption.p,
                salt = encryption.salt,
                nonce = encryption.nonce
            )
            BackupCrypto.decrypt(ciphertext, params, password)
        } catch (_: Exception) {
            return Failure(ImportFailure.WrongPassword)
        }

        try {
            val db = json.decodeFromString<MauthDb>(plaintext.toString(Charsets.UTF_8))
            val contents = BackupData(
                groups = db.groups.map { it.toDomain() },
                accounts = db.accounts.map { it.toDomain() }
            )
            return Success(
                accounts = contents.accounts,
                groups = contents.groups
            )
        } catch (_: Exception) {
            return Failure(ImportFailure.Malformed)
        } finally {
            plaintext.fill(0)
        }
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        val db = MauthDb(
            groups = contents.groups.map {
                MauthGroup(
                    id = it.id,
                    name = it.name,
                    emoji = it.emoji,
                    sortIndex = it.sortIndex
                )
            },
            accounts = contents.accounts.map {
                MauthAccount(
                    id = it.id,
                    name = it.name,
                    issuer = it.issuer,
                    secret = it.secret,
                    type = when (it.type) {
                        BackupOtpType.TOTP -> MauthOtpType.TOTP
                        BackupOtpType.HOTP -> MauthOtpType.HOTP
                    },
                    algorithm = when (it.algorithm) {
                        BackupOtpAlgorithm.SHA1 -> MauthOtpAlgorithm.SHA1
                        BackupOtpAlgorithm.SHA256 -> MauthOtpAlgorithm.SHA256
                        BackupOtpAlgorithm.SHA512 -> MauthOtpAlgorithm.SHA512
                    },
                    digits = it.digits,
                    period = it.period,
                    counter = it.counter,
                    group = it.group,
                    created = it.created
                )
            }
        )

        if (password == null) {
            return json.encodeToString(
                MauthPayload(
                    version = VERSION,
                    groups = db.groups,
                    accounts = db.accounts
                )
            )
        }

        val plaintext = json.encodeToByteArray(db)
        val blob = try {
            BackupCrypto.encrypt(plaintext, password)
        } finally {
            plaintext.fill(0)
        }

        return json.encodeToString(
            MauthPayload(
                version = VERSION,
                encryption = MauthEncryption(
                    m = blob.params.memoryKib,
                    t = blob.params.iterations,
                    p = blob.params.parallelism,
                    salt = blob.params.salt,
                    nonce = blob.params.nonce
                ),
                data = blob.ciphertext
            )
        )
    }

    private fun MauthGroup.toDomain(): BackupGroup {
        return BackupGroup(
            id = id,
            name = name,
            emoji = emoji,
            sortIndex = sortIndex
        )
    }

    private fun MauthAccount.toDomain(): BackupAccount {
        return BackupAccount(
            id = id,
            name = name,
            issuer = issuer,
            secret = secret,
            type = when (type) {
                MauthOtpType.TOTP -> BackupOtpType.TOTP
                MauthOtpType.HOTP -> BackupOtpType.HOTP
            },
            algorithm = when (algorithm) {
                MauthOtpAlgorithm.SHA1 -> BackupOtpAlgorithm.SHA1
                MauthOtpAlgorithm.SHA256 -> BackupOtpAlgorithm.SHA256
                MauthOtpAlgorithm.SHA512 -> BackupOtpAlgorithm.SHA512
            },
            digits = digits,
            period = period,
            counter = counter,
            group = group,
            created = created
        )
    }

}

@Serializable
private data class MauthPayload(
    val version: Int,
    val encryption: MauthEncryption? = null,
    @Serializable(with = Base64Bytes::class)
    val data: ByteArray? = null,
    val groups: List<MauthGroup>? = null,
    val accounts: List<MauthAccount>? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MauthPayload

        if (version != other.version) return false
        if (encryption != other.encryption) return false
        if (!data.contentEquals(other.data)) return false
        if (groups != other.groups) return false
        if (accounts != other.accounts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + (encryption?.hashCode() ?: 0)
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + (groups?.hashCode() ?: 0)
        result = 31 * result + (accounts?.hashCode() ?: 0)
        return result
    }
}

@Serializable
private data class MauthDb(
    val groups: List<MauthGroup> = emptyList(),
    val accounts: List<MauthAccount> = emptyList(),
)

@Serializable
private data class MauthEncryption(
    val kdf: String = KDF_ARGON2ID,
    val m: Int,
    val t: Int,
    val p: Int,
    @Serializable(with = Base64Bytes::class)
    val salt: ByteArray,
    @Serializable(with = Base64Bytes::class)
    val nonce: ByteArray,
) {
    companion object {
        const val KDF_ARGON2ID = "argon2id"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MauthEncryption

        if (m != other.m) return false
        if (t != other.t) return false
        if (p != other.p) return false
        if (kdf != other.kdf) return false
        if (!salt.contentEquals(other.salt)) return false
        if (!nonce.contentEquals(other.nonce)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = m
        result = 31 * result + t
        result = 31 * result + p
        result = 31 * result + kdf.hashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + nonce.contentHashCode()
        return result
    }
}

@Serializable
private data class MauthGroup(
    val id: String,
    val name: String,
    val emoji: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
private data class MauthAccount(
    val id: String,
    val name: String,
    val issuer: String = "",
    val secret: String,
    val type: MauthOtpType,
    val algorithm: MauthOtpAlgorithm = MauthOtpAlgorithm.SHA1,
    val digits: Int = 6,
    val period: Int? = null,
    val counter: Int? = null,
    val group: String? = null,
    val created: Long? = null,
)

@Serializable
private enum class MauthOtpType {
    @SerialName("totp")
    TOTP,

    @SerialName("hotp")
    HOTP,
}

@Serializable
private enum class MauthOtpAlgorithm {
    SHA1,
    SHA256,
    SHA512,
}
