package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

class StratumImporter : AccountImporter {

    override val source = ImportSource.Stratum

    private val json = Json { ignoreUnknownKeys = true }

    override fun sniff(content: String): Boolean {
        if (content.startsWith(HEADER) || content.startsWith(HEADER_LEGACY))
            return true

        val trimmed = content.trimStart()
        return trimmed.startsWith("{") && trimmed.contains("\"Authenticators\"")
    }

    override fun parse(content: String, password: CharArray?): ImportResult {
        if (content.startsWith(HEADER) || content.startsWith(HEADER_LEGACY)) {
            return ImportResult.Failure(source, ImportFailure.Unrecognized)
        }

        val backup = json.decodeFromString<StratumBackup>(content)
        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (entry in backup.authenticators) {
            val type = when (entry.type) {
                TYPE_HOTP -> BackupOtpType.HOTP
                TYPE_TOTP -> BackupOtpType.TOTP
                else -> {
                    // TODO: add Steam and Yandex generator support
                    rejected.add(
                        RejectedEntry(
                            name = entry.username.orEmpty(),
                            issuer = entry.issuer.orEmpty(),
                            reason = ImportFailure.Unrecognized
                        )
                    )
                    continue
                }
            }

            val secret = entry.secret?.trim()
            if (secret.isNullOrEmpty()) {
                rejected.add(
                    RejectedEntry(
                        name = entry.username.orEmpty(),
                        issuer = entry.issuer.orEmpty(),
                        reason = ImportFailure.InvalidSecret
                    )
                )
                continue
            }

            accounts.add(
                BackupAccount(
                    id = UUID.randomUUID().toString(),
                    name = entry.username.orEmpty(),
                    issuer = entry.issuer.orEmpty(),
                    secret = secret,
                    type = type,
                    algorithm = when (entry.algorithm) {
                        ALGORITHM_SHA256 -> BackupOtpAlgorithm.SHA256
                        ALGORITHM_SHA512 -> BackupOtpAlgorithm.SHA512
                        else -> BackupOtpAlgorithm.SHA1
                    },
                    digits = entry.digits,
                    period = if (type == BackupOtpType.TOTP) entry.period else null,
                    counter = if (type == BackupOtpType.HOTP) entry.counter else null
                )
            )
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(source, ImportFailure.Unrecognized)

        return ImportResult.Success(source, accounts, rejected = rejected)
    }

    @Serializable
    private data class StratumBackup(
        @SerialName("Authenticators") val authenticators: List<StratumAuthenticator> = emptyList()
    )

    @Serializable
    private data class StratumAuthenticator(
        @SerialName("Type") val type: Int = 0,
        @SerialName("Issuer") val issuer: String? = null,
        @SerialName("Username") val username: String? = null,
        @SerialName("Secret") val secret: String? = null,
        @SerialName("Algorithm") val algorithm: Int = 0,
        @SerialName("Digits") val digits: Int = 6,
        @SerialName("Period") val period: Int = 30,
        @SerialName("Counter") val counter: Int = 0
    )

    private companion object {
        const val HEADER = "AUTHENTICATORPRO"
        const val HEADER_LEGACY = "AuthenticatorPro"

        const val TYPE_HOTP = 1
        const val TYPE_TOTP = 2

        const val ALGORITHM_SHA256 = 1
        const val ALGORITHM_SHA512 = 2
    }
}
