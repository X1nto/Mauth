package com.xinto.mauth.core.backup.importer

import androidx.core.net.toUri
import com.xinto.mauth.core.backup.model.toBackupAccount
import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.otp.OtpUriParserResult
import com.xinto.mauth.core.otp.parseOtpUri

class UriListImporter : AccountImporter {

    override val source = ImportSource.UriList

    override fun sniff(content: String): Boolean {
        return lines(content).firstOrNull()?.isOtpUri() == true
    }

    override fun parse(content: String, password: CharArray?): ImportResult {
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
            return ImportResult.Failure(source, ImportFailure.Unrecognized)

        return ImportResult.Success(source = source, accounts = accounts, rejected = rejected)
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

    private companion object {
        const val BOM = "\uFEFF"
    }
}
