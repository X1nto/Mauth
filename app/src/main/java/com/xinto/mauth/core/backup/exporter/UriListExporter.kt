package com.xinto.mauth.core.backup.exporter

import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.toOtpData
import com.xinto.mauth.core.otp.exporter.OtpExporter

class UriListExporter(
    private val otpExporter: OtpExporter
) : BackupExporter {

    override fun export(contents: BackupData, password: CharArray?): String {
        return contents.accounts.joinToString("\n") {
            otpExporter.exportOtp(it.toOtpData())
        }
    }
}
