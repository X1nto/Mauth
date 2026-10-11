package com.xinto.mauth.core.backup

import com.xinto.mauth.core.otp.decodeOtpSecret
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base32
import java.util.UUID

object FreeOtpPlusBackup : BackupImporter, BackupExporter {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    override val mimeType = MimeType.Json

    override val supportsEncryption = false

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()
        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"tokens\"")
    }

    override fun import(content: String, password: CharArray?): ImportResult {
        val backup = json.decodeFromString<FreeOtpBackup>(content)

        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()
        val categories = linkedSetOf<String>()

        for (token in backup.tokens) {
            val secret = token.secretBase32?.takeIf { it.isNotBlank() } ?: token.secret?.let { encodeSecret(it) }

            if (secret.isNullOrBlank()) {
                rejected.add(
                    RejectedEntry(
                        name = token.label.orEmpty(),
                        issuer = token.issuerExt ?: token.issuerInt.orEmpty(),
                        reason = ImportFailure.InvalidSecret
                    )
                )
                continue
            }

            val type = BackupOtpType.fromName(token.type)
            val category = token.category?.takeIf { it.isNotBlank() }

            if (category != null) {
                categories.add(category)
            }

            accounts.add(
                BackupAccount(
                    id = UUID.randomUUID().toString(),
                    name = token.label.orEmpty(),
                    issuer = token.issuerExt ?: token.issuerInt.orEmpty(),
                    secret = secret,
                    type = type,
                    algorithm = BackupOtpAlgorithm.fromName(token.algo),
                    digits = token.digits ?: 6,
                    period = if (type == BackupOtpType.TOTP) token.period ?: 30 else null,
                    counter = if (type == BackupOtpType.HOTP) token.counter ?: 0 else null,
                    group = category
                )
            )
        }

        if (accounts.isEmpty() && rejected.isEmpty())
            return ImportResult.Failure(ImportFailure.Unrecognized)

        return ImportResult.Success(
            accounts = accounts,
            groups = categories.map { BackupGroup(id = it, name = it) },
            rejected = rejected
        )
    }

    override fun export(contents: BackupData, password: CharArray?): String {
        val groupNames = contents.groups.associate { it.id to it.name }

        return json.encodeToString(
            FreeOtpBackup(
                tokens = contents.accounts.map { it.toToken(groupNames[it.group]) },
                tokenOrder = contents.accounts.map { it.tokenId }
            )
        )
    }

    private fun encodeSecret(signedBytes: List<Int>): String {
        val bytes = ByteArray(signedBytes.size) {
            (signedBytes[it] and 0xFF).toByte()
        }
        return Base32().encodeAsString(bytes).trimEnd('=')
    }

    private fun BackupAccount.toToken(group: String?): FreeOtpToken {
        return FreeOtpToken(
            issuerExt = issuer,
            label = name,
            type = when (type) {
                BackupOtpType.TOTP -> "TOTP"
                BackupOtpType.HOTP -> "HOTP"
            },
            algo = algorithm.name,
            secret = secret.decodeOtpSecret().map { it.toInt() },
            digits = digits,
            counter = counter ?: 0,
            period = period ?: 30,
            category = group
        )
    }

    private val BackupAccount.tokenId: String
        get() = if (issuer.isNotBlank()) "$issuer:$name" else name
}

@Serializable
private data class FreeOtpBackup(
    val tokens: List<FreeOtpToken> = emptyList(),
    val tokenOrder: List<String> = emptyList()
)

@Serializable
private data class FreeOtpToken(
    val secret: List<Int>? = null,
    @SerialName("secret_base32")
    val secretBase32: String? = null,
    val algo: String? = null,
    val digits: Int? = null,
    val counter: Int? = null,
    val period: Int? = null,
    val issuerExt: String? = null,
    val issuerInt: String? = null,
    val label: String? = null,
    val type: String? = null,
    val category: String? = null
)
