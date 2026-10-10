package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupGroup
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.backup.schema.FreeOtpBackup
import com.xinto.mauth.core.backup.schema.FreeOtpPlusSchema
import org.apache.commons.codec.binary.Base32
import java.util.UUID

class FreeOtpPlusImporter : AccountImporter {

    override val source = ImportSource.FreeOtpPlus

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()
        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"tokens\"")
    }

    override fun parse(content: String, password: CharArray?): ImportResult {
        val backup = FreeOtpPlusSchema.json.decodeFromString<FreeOtpBackup>(content)

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
            return ImportResult.Failure(source, ImportFailure.Unrecognized)

        return ImportResult.Success(
            source = source,
            accounts = accounts,
            groups = categories.map { BackupGroup(id = it, name = it) },
            rejected = rejected
        )
    }

    private fun encodeSecret(signedBytes: List<Int>): String {
        val bytes = ByteArray(signedBytes.size) {
            (signedBytes[it] and 0xFF).toByte()
        }
        return Base32().encodeAsString(bytes).trimEnd('=')
    }
}
