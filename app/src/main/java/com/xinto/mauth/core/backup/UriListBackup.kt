package com.xinto.mauth.core.backup

import androidx.core.net.toUri
import com.xinto.mauth.core.otp.OtpUriParserResult
import com.xinto.mauth.core.otp.parseOtpUri
import com.xinto.mauth.core.otp.toOtpUri

object UriListBackup : BackupImporter, BackupExporter {

    private const val BOM = "\uFEFF"

    override val mimeType = MimeType.PlainText

    override val supportsEncryption = false

    override fun sniff(content: String): Boolean {
        return lines(content).firstOrNull()?.isOtpUri() == true
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (line in lines(content)) {
            if (!line.isOtpUri()) {
                rejected.add(RejectedEntry("", "", ImportFailure.Unrecognized))
                continue
            }

            when (val result = line.parseOtpUri()) {
                is OtpUriParserResult.Success -> {
                    accounts.add(result.data.toBackupAccount())
                }
                is OtpUriParserResult.Multipart -> {
                    result.data.forEach {
                        accounts.add(it.toBackupAccount())
                    }
                }
                is OtpUriParserResult.Failure -> {
                    val uri = line.toUri()
                    val label = uri.pathSegments.firstOrNull().orEmpty()
                    rejected.add(
                        RejectedEntry(
                            name = label.substringAfter(':').trim(),
                            issuer = uri.getQueryParameter("issuer")
                                ?: label.substringBefore(':', missingDelimiterValue = "").trim(),
                            reason = ImportFailure.Malformed
                        )
                    )
                }
            }
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(ImportFailure.Unrecognized)

        return ImportResult.Success(accounts = accounts, rejected = rejected)
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        return contents.accounts.joinToString("\n") {
            it.toOtpData().toOtpUri()
        }
    }

    private fun lines(content: String): Sequence<String> {
        return content.removePrefix(BOM)
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun String.isOtpUri(): Boolean {
        return startsWith("otpauth://", ignoreCase = true) || startsWith("otpauth-migration://", ignoreCase = true)
    }
}
