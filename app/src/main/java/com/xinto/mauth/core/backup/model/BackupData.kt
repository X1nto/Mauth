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
    HOTP,
}

enum class BackupOtpAlgorithm {
    SHA1,
    SHA256,
    SHA512,
}