package com.xinto.mauth.core.backup.exporter

import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.backup.schema.FreeOtpBackup
import com.xinto.mauth.core.backup.schema.FreeOtpPlusSchema
import com.xinto.mauth.core.backup.schema.FreeOtpToken
import com.xinto.mauth.core.otp.transformer.KeyTransformer

class FreeOtpPlusExporter(
    private val keyTransformer: KeyTransformer
) : BackupExporter {

    override fun export(contents: BackupData, password: CharArray?): String {
        val groupNames = contents.groups.associate { it.id to it.name }

        return FreeOtpPlusSchema.json.encodeToString(
            FreeOtpBackup(
                tokens = contents.accounts.map { it.toToken(groupNames[it.group]) },
                tokenOrder = contents.accounts.map { it.tokenId }
            )
        )
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
            secret = keyTransformer.transformToBytes(secret).map { it.toInt() },
            digits = digits,
            counter = counter ?: 0,
            period = period ?: 30,
            category = group
        )
    }

    private val BackupAccount.tokenId: String
        get() = if (issuer.isNotBlank()) "$issuer:$name" else name
}
