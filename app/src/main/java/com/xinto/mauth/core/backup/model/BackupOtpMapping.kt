package com.xinto.mauth.core.backup.model

import com.xinto.mauth.core.otp.OtpData
import com.xinto.mauth.core.otp.OtpDigest
import com.xinto.mauth.core.otp.OtpType
import java.util.UUID

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
