package com.xinto.mauth.core.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

object StratumBackup : BackupImporter, BackupExporter {

    private const val HEADER = "AUTHENTICATORPRO"
    private const val HEADER_LEGACY = "AuthenticatorPro"

    private const val TYPE_HOTP = 1
    private const val TYPE_TOTP = 2

    private const val ALGORITHM_SHA1 = 0
    private const val ALGORITHM_SHA256 = 1
    private const val ALGORITHM_SHA512 = 2

    private const val ISSUER_MAX_LENGTH = 32
    private const val USERNAME_MAX_LENGTH = 40
    private const val CATEGORY_NAME_MAX_LENGTH = 32
    private const val CATEGORY_ID_LENGTH = 8

    private val TOTP_DIGITS = 6..10
    private val HOTP_DIGITS = 6..8

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val mimeType = MimeType.Json

    override val supportsEncryption = false

    override fun sniff(content: String): Boolean {
        if (content.startsWith(HEADER) || content.startsWith(HEADER_LEGACY))
            return true

        val trimmed = content.trimStart()
        return trimmed.startsWith("{") && trimmed.contains("\"Authenticators\"")
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        if (content.startsWith(HEADER) || content.startsWith(HEADER_LEGACY)) {
            return ImportResult.Failure(ImportFailure.Unrecognized)
        }

        val backup = json.decodeFromString<StratumBackupFile>(content)
        val groups = backup.categories.map { BackupGroup(id = it.id, name = it.name, sortIndex = it.ranking) }
        val groupIds = groups.map { it.id }.toSet()

        val accountGroups = backup.authenticatorCategories
            .filter { it.categoryId in groupIds }
            .reversed()
            .associate { it.authenticatorSecret to it.categoryId }

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
                    counter = if (type == BackupOtpType.HOTP) entry.counter else null,
                    group = accountGroups[entry.secret]
                )
            )
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(ImportFailure.Unrecognized)

        return ImportResult.Success(accounts, groups = groups, rejected = rejected)
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        val categoryIds = mutableMapOf<String, String>()
        val categories = contents.groups.map {
            val name = it.name.trim().take(CATEGORY_NAME_MAX_LENGTH)
            val category = StratumCategory(
                id = BackupCrypto.sha1(name.toByteArray(Charsets.UTF_8)).toHexString().take(CATEGORY_ID_LENGTH),
                name = name,
                ranking = it.sortIndex
            )
            categoryIds[it.id] = category.id
            category
        }

        // Stratum aborts the whole restore on the first entry it can't validate
        val accounts = contents.accounts.filter {
            when (it.type) {
                BackupOtpType.TOTP -> it.digits in TOTP_DIGITS && (it.period ?: 30) > 0
                BackupOtpType.HOTP -> it.digits in HOTP_DIGITS
            }
        }
        val authenticators = accounts.mapIndexed { index, account ->
            account.toAuthenticator(ranking = index)
        }

        val bindings = accounts.zip(authenticators).mapNotNull { (account, authenticator) ->
            val categoryId = categoryIds[account.group]
                ?: return@mapNotNull null

            StratumAuthenticatorCategory(
                categoryId = categoryId,
                authenticatorSecret = authenticator.secret.orEmpty()
            )
        }

        return json.encodeToString(
            StratumBackupFile(
                authenticators = authenticators,
                categories = categories,
                authenticatorCategories = bindings
            )
        )
    }

    private fun BackupAccount.toAuthenticator(ranking: Int): StratumAuthenticator {
        // Stratum requires an issuer, so an account without one moves its name there
        val username = if (issuer.isNotBlank()) name else ""
        val issuer = issuer.ifBlank { name }

        return StratumAuthenticator(
            type = when (type) {
                BackupOtpType.HOTP -> TYPE_HOTP
                BackupOtpType.TOTP -> TYPE_TOTP
            },
            issuer = issuer.take(ISSUER_MAX_LENGTH),
            username = username.take(USERNAME_MAX_LENGTH),
            secret = secret,
            algorithm = when (algorithm) {
                BackupOtpAlgorithm.SHA1 -> ALGORITHM_SHA1
                BackupOtpAlgorithm.SHA256 -> ALGORITHM_SHA256
                BackupOtpAlgorithm.SHA512 -> ALGORITHM_SHA512
            },
            digits = digits,
            period = period ?: 30,
            counter = counter ?: 0,
            ranking = ranking
        )
    }

    @Serializable
    private data class StratumBackupFile(
        @SerialName("Authenticators") val authenticators: List<StratumAuthenticator> = emptyList(),
        @SerialName("Categories") val categories: List<StratumCategory> = emptyList(),
        @SerialName("AuthenticatorCategories") val authenticatorCategories: List<StratumAuthenticatorCategory> = emptyList()
    )

    @Serializable
    private data class StratumCategory(
        @SerialName("Id") val id: String,
        @SerialName("Name") val name: String,
        @SerialName("Ranking") val ranking: Int = 0
    )

    @Serializable
    private data class StratumAuthenticatorCategory(
        @SerialName("CategoryId") val categoryId: String,
        @SerialName("AuthenticatorSecret") val authenticatorSecret: String,
        @SerialName("Ranking") val ranking: Int = 0
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
        @SerialName("Counter") val counter: Int = 0,
        @SerialName("Ranking") val ranking: Int = 0
    )
}
