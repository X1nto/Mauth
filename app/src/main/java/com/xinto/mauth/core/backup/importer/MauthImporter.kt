package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.crypto.Argon2Params
import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.core.backup.importer.ImportResult.Failure
import com.xinto.mauth.core.backup.importer.ImportResult.Success
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
import kotlin.collections.map
import kotlin.collections.orEmpty

class MauthImporter(private val crypto: BackupCrypto) : AccountImporter {

    override val source = ImportSource.Mauth

    override fun sniff(content: String): Boolean {
        val trimmed = content.trimStart()

        if (!trimmed.startsWith("{"))
            return false

        return trimmed.contains("\"accounts\"") || trimmed.contains("\"encryption\"")
    }

    override fun parse(content: String, password: CharArray?): ImportResult {
        val payload = try {
            MauthSchema.json.decodeFromString<MauthPayload>(content)
        } catch (e: Exception) {
            return Failure(source, ImportFailure.Unrecognized)
        }

        if (payload.version > MauthSchema.VERSION)
            return Failure(source, ImportFailure.UnsupportedVersion)

        val encryption = payload.encryption
        if (encryption == null) {
            if (payload.accounts == null)
                return Failure(source, ImportFailure.Unrecognized)

            return Success(
                source = source,
                accounts = payload.accounts.map { it.toDomain() },
                groups = payload.groups.orEmpty().map { it.toDomain() }
            )
        }

        if (password == null)
            return ImportResult.NeedsPassword(source)

        if (encryption.kdf != MauthEncryption.KDF_ARGON2ID)
            return Failure(source, ImportFailure.UnsupportedVersion)

        val ciphertext = payload.data
            ?: return Failure(source, ImportFailure.Malformed)

        val plaintext = try {
            val params = Argon2Params(
                memoryKib = encryption.m,
                iterations = encryption.t,
                parallelism = encryption.p,
                salt = encryption.salt,
                nonce = encryption.nonce
            )
            crypto.decrypt(ciphertext, params, password)
        } catch (_: Exception) {
            return Failure(source, ImportFailure.WrongPassword)
        }

        try {
            val db = MauthSchema.json.decodeFromString<MauthDb>(plaintext.toString(Charsets.UTF_8))
            val contents = BackupData(
                groups = db.groups.map { it.toDomain() },
                accounts = db.accounts.map { it.toDomain() }
            )
            return Success(
                source = source,
                accounts = contents.accounts,
                groups = contents.groups
            )
        } catch (_: Exception) {
            return Failure(source, ImportFailure.Malformed)
        } finally {
            plaintext.fill(0)
        }
    }

    private fun MauthGroup.toDomain(): BackupGroup {
        return BackupGroup(
            id = id,
            name = name,
            emoji = emoji,
            sortIndex = sortIndex
        )
    }

    private fun MauthAccount.toDomain(): BackupAccount {
        return BackupAccount(
            id = id,
            name = name,
            issuer = issuer,
            secret = secret,
            type = when (type) {
                MauthOtpType.TOTP -> BackupOtpType.TOTP
                MauthOtpType.HOTP -> BackupOtpType.HOTP
            },
            algorithm = when (algorithm) {
                MauthOtpAlgorithm.SHA1 -> BackupOtpAlgorithm.SHA1
                MauthOtpAlgorithm.SHA256 -> BackupOtpAlgorithm.SHA256
                MauthOtpAlgorithm.SHA512 -> BackupOtpAlgorithm.SHA512
            },
            digits = digits,
            period = period,
            counter = counter,
            group = group,
            created = created
        )
    }
}
