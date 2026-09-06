package com.xinto.mauth.core.backup.exporter

import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.otp.exporter.OtpExporter
import com.xinto.mauth.core.otp.model.OtpData
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType

class UriListExporter(
    private val otpExporter: OtpExporter
) : BackupExporter {

    override fun export(contents: BackupData, password: CharArray?): String {
        return contents.accounts.joinToString("\n") {
            otpExporter.exportOtp(it.toOtpData())
        }
    }

    private fun BackupAccount.toOtpData(): OtpData {
        return OtpData(
            label = name,
            issuer = issuer,
            secret = secret,
            algorithm = when (algorithm) {
                BackupOtpAlgorithm.SHA1 -> OtpDigest.SHA1
                BackupOtpAlgorithm.SHA256 -> OtpDigest.SHA256
                BackupOtpAlgorithm.SHA512 -> OtpDigest.SHA512
            },
            type = when (type) {
                BackupOtpType.TOTP -> OtpType.TOTP
                BackupOtpType.HOTP -> OtpType.HOTP
            },
            digits = digits,
            counter = counter,
            period = period
        )
    }
}
