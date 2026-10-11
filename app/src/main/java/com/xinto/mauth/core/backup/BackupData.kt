package com.xinto.mauth.core.backup

import com.xinto.mauth.core.otp.OtpData
import com.xinto.mauth.core.otp.OtpDigest
import com.xinto.mauth.core.otp.OtpType
import java.util.UUID

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

fun OtpType.toBackupOtpType(): BackupOtpType {
    return when (this) {
        OtpType.TOTP -> BackupOtpType.TOTP
        OtpType.HOTP -> BackupOtpType.HOTP
    }
}

fun BackupOtpType.toOtpType(): OtpType {
    return when (this) {
        BackupOtpType.TOTP -> OtpType.TOTP
        BackupOtpType.HOTP -> OtpType.HOTP
    }
}

fun OtpDigest.toBackupOtpAlgorithm(): BackupOtpAlgorithm {
    return when (this) {
        OtpDigest.SHA1 -> BackupOtpAlgorithm.SHA1
        OtpDigest.SHA256 -> BackupOtpAlgorithm.SHA256
        OtpDigest.SHA512 -> BackupOtpAlgorithm.SHA512
    }
}

fun BackupOtpAlgorithm.toOtpDigest(): OtpDigest {
    return when (this) {
        BackupOtpAlgorithm.SHA1 -> OtpDigest.SHA1
        BackupOtpAlgorithm.SHA256 -> OtpDigest.SHA256
        BackupOtpAlgorithm.SHA512 -> OtpDigest.SHA512
    }
}

fun OtpData.toBackupAccount(group: String? = null): BackupAccount {
    return BackupAccount(
        id = UUID.randomUUID().toString(),
        name = label,
        issuer = issuer,
        secret = secret,
        type = type.toBackupOtpType(),
        algorithm = algorithm.toBackupOtpAlgorithm(),
        digits = digits,
        period = if (type == OtpType.TOTP) period ?: 30 else null,
        counter = if (type == OtpType.HOTP) counter ?: 0 else null,
        group = group
    )
}

fun BackupAccount.toOtpData(): OtpData {
    return OtpData(
        label = name,
        issuer = issuer,
        secret = secret,
        algorithm = algorithm.toOtpDigest(),
        type = type.toOtpType(),
        digits = digits,
        counter = counter,
        period = period
    )
}