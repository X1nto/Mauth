package com.xinto.mauth.core.backup.schema

import com.xinto.mauth.ui.util.Base64Bytes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object MauthSchema {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    const val VERSION = 1
}

@Serializable
data class MauthPayload(
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
data class MauthDb(
    val groups: List<MauthGroup> = emptyList(),
    val accounts: List<MauthAccount> = emptyList(),
)

@Serializable
data class MauthEncryption(
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
data class MauthGroup(
    val id: String,
    val name: String,
    val emoji: String? = null,
    val sortIndex: Int = 0,
)

@Serializable
data class MauthAccount(
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
enum class MauthOtpType {
    @SerialName("totp")
    TOTP,

    @SerialName("hotp")
    HOTP,
}

@Serializable
enum class MauthOtpAlgorithm {
    SHA1,
    SHA256,
    SHA512,
}
