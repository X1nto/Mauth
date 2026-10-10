package com.xinto.mauth.core.backup.model

data class BackupData(
    val groups: List<BackupGroup> = emptyList(),
    val accounts: List<BackupAccount> = emptyList(),
)

data class BackupGroup(
    val id: String,
    val name: String,
    val emoji: String? = null,
    val sortIndex: Int = 0,
)

data class BackupAccount(
    val id: String,
    val name: String,
    val issuer: String = "",
    val secret: String,
    val type: BackupOtpType,
    val algorithm: BackupOtpAlgorithm = BackupOtpAlgorithm.SHA1,
    val digits: Int = 6,
    val period: Int? = null,
    val counter: Int? = null,
    val group: String? = null,
    val created: Long? = null,
)

enum class BackupOtpType {
    TOTP,
    HOTP;

    companion object {
        fun fromName(name: String?): BackupOtpType {
            return if (name?.lowercase()?.contains("hotp") == true) HOTP else TOTP
        }
    }
}

enum class BackupOtpAlgorithm {
    SHA1,
    SHA256,
    SHA512;

    companion object {
        fun fromName(name: String?): BackupOtpAlgorithm {
            return when (name?.uppercase()?.removePrefix("HMAC-")?.replace("-", "")) {
                "SHA256" -> SHA256
                "SHA512" -> SHA512
                else -> SHA1
            }
        }
    }
}