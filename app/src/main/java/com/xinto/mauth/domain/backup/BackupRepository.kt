package com.xinto.mauth.domain.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.xinto.mauth.core.backup.exporter.AegisExporter
import com.xinto.mauth.core.backup.exporter.FreeOtpPlusExporter
import com.xinto.mauth.core.backup.exporter.MauthExporter
import com.xinto.mauth.core.backup.exporter.UriListExporter
import com.xinto.mauth.core.backup.importer.ImportFailure
import com.xinto.mauth.core.backup.importer.ImportResult
import com.xinto.mauth.core.backup.importer.ImportSource
import com.xinto.mauth.core.backup.importer.ImporterRegistry
import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupData
import com.xinto.mauth.core.backup.model.BackupFormat
import com.xinto.mauth.core.backup.model.BackupGroup
import com.xinto.mauth.core.backup.model.toBackupOtpAlgorithm
import com.xinto.mauth.core.backup.model.toBackupOtpType
import com.xinto.mauth.core.backup.model.toOtpDigest
import com.xinto.mauth.core.backup.model.toOtpType
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.core.otp.transformer.KeyTransformer
import com.xinto.mauth.db.AccountDatabase
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.account.entity.EntityAccount
import com.xinto.mauth.db.dao.group.GroupsDao
import com.xinto.mauth.db.dao.group.entity.EntityGroup
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.backup.model.DomainImportCandidate
import com.xinto.mauth.domain.backup.model.DomainRejectedEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class BackupRepository(
    private val context: Context,
    private val database: AccountDatabase,
    private val accountsDao: AccountsDao,
    private val groupsDao: GroupsDao,
    private val rtdataDao: RtdataDao,
    private val mauthExporter: MauthExporter,
    private val aegisExporter: AegisExporter,
    private val freeOtpPlusExporter: FreeOtpPlusExporter,
    private val uriListExporter: UriListExporter,
    private val importerRegistry: ImporterRegistry,
    private val keyTransformer: KeyTransformer
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

    suspend fun import(uri: Uri, source: ImportSource, password: CharArray?): ImportOutcome {
        return withContext(Dispatchers.IO) {
            val content = try {
                context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (content == null) {
                return@withContext ImportOutcome.Failed(source, ImportFailure.Malformed)
            }

            try {
                when (val result = importerRegistry.parse(source, content, password)) {
                    is ImportResult.NeedsPassword -> ImportOutcome.NeedsPassword(result.source)
                    is ImportResult.WrongFormat -> ImportOutcome.WrongFormat(result.expected, result.actual)
                    is ImportResult.Failure -> ImportOutcome.Failed(result.source, result.reason)
                    is ImportResult.Success -> toCandidates(result)
                }
            } finally {
                password?.fill('\u0000')
            }
        }
    }

    suspend fun commit(candidates: List<DomainImportCandidate>) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val resolvedGroups = mutableMapOf<String, UUID>()

                for (candidate in candidates) {
                    val name = candidate.groupName?.trim()
                    if (name.isNullOrEmpty() || resolvedGroups.containsKey(name))
                        continue

                    val existing = groupsDao.getByName(name)
                    if (existing != null) {
                        resolvedGroups[name] = existing.id
                    } else {
                        val group = EntityGroup(
                            name = name,
                            emoji = candidate.groupEmoji,
                            sortIndex = groupsDao.nextSortIndex()
                        )
                        groupsDao.upsert(group)
                        resolvedGroups[name] = group.id
                    }
                }

                for ((account, groupName) in candidates) {
                    val groupId = groupName?.trim()?.let(resolvedGroups::get)
                    accountsDao.upsert(account.toEntityAccount(groupId))
                    rtdataDao.upsertCountData(EntityCountData(account.id, account.counter))
                }
            }
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

    private suspend fun toCandidates(result: ImportResult.Success): ImportOutcome {
        val existingKeys = accountsDao.getAll()
            .map { dedupKeyOf(it.issuer, it.label, it.secret) }
            .toSet()
        val groupsById = result.groups.associateBy { it.id }

        val candidates = mutableListOf<DomainImportCandidate>()
        val rejected = result.rejected.map {
            DomainRejectedEntry(
                name = it.name,
                issuer = it.issuer,
                reason = it.reason
            )
        }.toMutableList()

        for (parsed in result.accounts) {
            val account = when {
                parsed.name.isNotBlank() -> parsed
                parsed.issuer.isNotBlank() -> parsed.copy(name = parsed.issuer, issuer = "")
                else -> {
                    rejected.add(
                        DomainRejectedEntry(
                            name = "",
                            issuer = "",
                            reason = ImportFailure.MissingName
                        )
                    )
                    continue
                }
            }

            val secret = account.secret.trim()
            if (keyTransformer.transformToBytes(secret).isEmpty()) {
                rejected.add(
                    DomainRejectedEntry(
                        name = account.name,
                        issuer = account.issuer,
                        reason = ImportFailure.InvalidSecret
                    )
                )
                continue
            }

            val group = account.group?.let(groupsById::get)
            candidates.add(
                DomainImportCandidate(
                    account = account.toDomainAccountInfo(secret),
                    groupName = group?.name,
                    groupEmoji = group?.emoji,
                    isDuplicate = dedupKeyOf(account.issuer, account.name, secret) in existingKeys
                )
            )
        }

        return ImportOutcome.Parsed(
            source = result.source,
            candidates = candidates,
            rejected = rejected
        )
    }

    private fun dedupKeyOf(issuer: String, label: String, secret: String): String {
        return listOf(issuer.lowercase(), label.lowercase(), secret.uppercase()).joinToString("\u0000")
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
            type = type.toBackupOtpType(),
            algorithm = algorithm.toBackupOtpAlgorithm(),
            digits = digits,
            period = if (type == OtpType.TOTP) period else null,
            counter = if (type == OtpType.HOTP) counter else null,
            group = groupId?.toString(),
            created = createDateMillis
        )
    }

    private fun BackupAccount.toDomainAccountInfo(secret: String): DomainAccountInfo {
        return DomainAccountInfo(
            id = id.toUuidOrRandom(),
            icon = null,
            label = name,
            issuer = issuer,
            secret = secret,
            algorithm = algorithm.toOtpDigest(),
            type = type.toOtpType(),
            digits = digits,
            counter = counter ?: 0,
            period = period ?: 30,
            groupId = null,
            createdMillis = created ?: System.currentTimeMillis()
        )
    }

    private fun DomainAccountInfo.toEntityAccount(groupId: UUID?): EntityAccount {
        return EntityAccount(
            id = id,
            icon = icon,
            secret = secret,
            label = label,
            issuer = issuer,
            algorithm = algorithm,
            type = type,
            digits = digits,
            period = period,
            groupId = groupId,
            createDateMillis = createdMillis
        )
    }

    private fun String.toUuidOrRandom(): UUID {
        return try {
            UUID.fromString(this)
        } catch (_: IllegalArgumentException) {
            UUID.randomUUID()
        }
    }

}
