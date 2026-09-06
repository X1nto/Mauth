package com.xinto.mauth.domain.backup

import android.content.Context
import android.net.Uri
import com.xinto.mauth.core.backup.exporter.AegisExporter
import com.xinto.mauth.core.backup.exporter.FreeOtpPlusExporter
import com.xinto.mauth.core.backup.exporter.MauthExporter
import com.xinto.mauth.core.backup.exporter.UriListExporter
import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.BackupFormat
import com.xinto.mauth.core.backup.model.BackupGroup
import com.xinto.mauth.core.backup.model.BackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.BackupOtpType
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.account.entity.EntityAccount
import com.xinto.mauth.db.dao.group.GroupsDao
import com.xinto.mauth.db.dao.group.entity.EntityGroup
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class BackupRepository(
    private val context: Context,
    private val accountsDao: AccountsDao,
    private val groupsDao: GroupsDao,
    private val rtdataDao: RtdataDao,
    private val mauthExporter: MauthExporter,
    private val aegisExporter: AegisExporter,
    private val freeOtpPlusExporter: FreeOtpPlusExporter,
    private val uriListExporter: UriListExporter
) {

    suspend fun export(
        format: BackupFormat,
        target: Uri,
        password: CharArray?,
        accountIds: List<UUID>
    ) {
        if (!format.supportsEncryption && password != null) {
            throw IllegalArgumentException("$format does not support encryption")
        }

        if (accountIds.isEmpty()) {
            throw IllegalArgumentException("accountIds can not be empty")
        }

        withContext(Dispatchers.IO) {
            val contents = readContents(accountIds)
            val serialized = try {
                when (format) {
                    BackupFormat.MauthJson -> mauthExporter.export(contents, password)
                    BackupFormat.AegisVault -> aegisExporter.export(contents, password)
                    BackupFormat.FreeOtpPlusJson -> freeOtpPlusExporter.export(contents, password)
                    BackupFormat.UriList -> uriListExporter.export(contents, password)
                }
            } finally {
                password?.fill('\u0000')
            }

            context.contentResolver.openOutputStream(target, "wt")?.use { stream ->
                stream.write(serialized.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: throw IllegalStateException("Could not open the file for writing")
        }
    }

    private suspend fun readContents(accountIds: List<UUID>): BackupData {
        val accounts = accountsDao.getAll().filter { it.id in accountIds }
        val counters = rtdataDao.getAll().associate { it.accountId to it.count }
        val groups = groupsDao.getAll()

        val groupIds = accounts.mapNotNull { it.groupId }.toSet()

        return BackupData(
            groups = groups
                .filter { it.id in groupIds }
                .map { it.toBackupGroup() },
            accounts = accounts.map { it.toBackupAccount(counters[it.id] ?: 0) }
        )
    }

    private fun EntityGroup.toBackupGroup(): BackupGroup {
        return BackupGroup(
            id = id.toString(),
            name = name,
            emoji = emoji,
            sortIndex = sortIndex
        )
    }

    private fun EntityAccount.toBackupAccount(counter: Int): BackupAccount {
        return BackupAccount(
            id = id.toString(),
            name = label,
            issuer = issuer,
            secret = secret,
            type = when (type) {
                OtpType.TOTP -> BackupOtpType.TOTP
                OtpType.HOTP -> BackupOtpType.HOTP
            },
            algorithm = when (algorithm) {
                OtpDigest.SHA1 -> BackupOtpAlgorithm.SHA1
                OtpDigest.SHA256 -> BackupOtpAlgorithm.SHA256
                OtpDigest.SHA512 -> BackupOtpAlgorithm.SHA512
            },
            digits = digits,
            period = if (type == OtpType.TOTP) period else null,
            counter = if (type == OtpType.HOTP) counter else null,
            group = groupId?.toString(),
            created = createDateMillis
        )
    }

}
