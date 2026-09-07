package com.xinto.mauth.core.backup.exporter

import com.xinto.mauth.core.backup.crypto.Argon2Params
import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.ui.util.encodeToByteArray
import com.xinto.mauth.core.backup.schema.MauthAccount
import com.xinto.mauth.core.backup.schema.MauthDb
import com.xinto.mauth.core.backup.schema.MauthEncryption
import com.xinto.mauth.core.backup.schema.MauthGroup
import com.xinto.mauth.core.backup.schema.MauthOtpAlgorithm
import com.xinto.mauth.core.backup.schema.MauthOtpType
import com.xinto.mauth.core.backup.schema.MauthPayload
import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.BackupGroup
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.backup.schema.MauthSchema

class MauthExporter(
    private val crypto: BackupCrypto
) : BackupExporter {

    override fun export(contents: BackupData, password: CharArray?): String {
        val db = contents.toDb()

        if (password == null) {
            return MauthSchema.json.encodeToString(
                MauthPayload(
                    version = MauthSchema.VERSION,
                    groups = db.groups,
                    accounts = db.accounts
                )
            )
        }

        val plaintext = MauthSchema.json.encodeToByteArray(db)
        val blob = try {
            crypto.encrypt(plaintext, password)
        } finally {
            plaintext.fill(0)
        }

        return MauthSchema.json.encodeToString(
            MauthPayload(
                version = MauthSchema.VERSION,
                encryption = blob.params.toWire(),
                data = blob.ciphertext
            )
        )
    }

    private fun BackupData.toDb(): MauthDb {
        return MauthDb(
            groups = groups.map { it.toWire() },
            accounts = accounts.map { it.toWire() }
        )
    }

    private fun BackupGroup.toWire(): MauthGroup {
        return MauthGroup(
            id = id,
            name = name,
            emoji = emoji,
            sortIndex = sortIndex
        )
    }

    private fun BackupAccount.toWire(): MauthAccount {
        return MauthAccount(
            id = id,
            name = name,
            issuer = issuer,
            secret = secret,
            type = when (type) {
                BackupOtpType.TOTP -> MauthOtpType.TOTP
                BackupOtpType.HOTP -> MauthOtpType.HOTP
            },
            algorithm = when (algorithm) {
                BackupOtpAlgorithm.SHA1 -> MauthOtpAlgorithm.SHA1
                BackupOtpAlgorithm.SHA256 -> MauthOtpAlgorithm.SHA256
                BackupOtpAlgorithm.SHA512 -> MauthOtpAlgorithm.SHA512
            },
            digits = digits,
            period = period,
            counter = counter,
            group = group,
            created = created
        )
    }

    private fun Argon2Params.toWire(): MauthEncryption {
        return MauthEncryption(
            m = memoryKib,
            t = iterations,
            p = parallelism,
            salt = salt,
            nonce = nonce
        )
    }
}
