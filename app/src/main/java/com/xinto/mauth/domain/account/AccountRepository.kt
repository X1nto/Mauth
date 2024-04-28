package com.xinto.mauth.domain.account

import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.account.entity.EntityAccount
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData
import com.xinto.mauth.domain.SettingsRepository
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AccountRepository(
    private val accountsDao: AccountsDao,
    private val rtdataDao: RtdataDao,
    private val settingsRepository: SettingsRepository
) {

    fun getAccounts(): Flow<List<DomainAccount>> {
        return combine(
            accountsDao.observeAll(),
            settingsRepository.getSortMode()
        ) { accounts, sort ->
            val mapped = accounts.map {
                it.toDomain()
            }
            return@combine when (sort) {
                SortSetting.IssuerAsc -> mapped.sortedBy { it.issuer }
                SortSetting.IssuerDesc -> mapped.sortedByDescending { it.issuer }
                SortSetting.DateAsc -> mapped.sortedBy { it.createdMillis }
                SortSetting.DateDesc -> mapped.sortedByDescending { it.createdMillis }
                SortSetting.LabelAsc -> mapped.sortedBy { it.label }
                SortSetting.LabelDesc -> mapped.sortedByDescending { it.label }
            }
        }
    }

    fun getAccountInfo(id: UUID): Flow<DomainAccountInfo> {
        return flow {
            val account = accountsDao.getById(id)
            if (account != null) {
                val counter = rtdataDao.getAccountCounter(id)
                emit(account.toDomainAccountInfo(counter))
            } else {
                throw NoSuchElementException()
            }
        }
    }

    suspend fun putAccount(domainAccountInfo: DomainAccountInfo) {
        val entityAccount = domainAccountInfo.toEntityAccount()
        rtdataDao.upsertCountData(EntityCountData(entityAccount.id, domainAccountInfo.counter.toInt()))
        accountsDao.upsert(entityAccount)
    }

    suspend fun incrementAccountCounter(id: UUID) {
        rtdataDao.incrementAccountCounter(id)
    }

    suspend fun deleteAccounts(ids: List<UUID>) {
        accountsDao.delete(ids.toSet())
    }

    private fun EntityAccount.toDomain(): DomainAccount {
        return when (type) {
            OtpType.TOTP -> {
                DomainAccount.Totp(
                    id = id,
                    icon = icon,
                    secret = secret,
                    label = label,
                    issuer = issuer,
                    algorithm = algorithm,
                    digits = digits,
                    period = period,
                    createdMillis = createDateMillis
                )
            }
            OtpType.HOTP -> {
                DomainAccount.Hotp(
                    id = id,
                    secret = secret,
                    icon = icon,
                    label = label,
                    issuer = issuer,
                    algorithm = algorithm,
                    digits = digits,
                    createdMillis = createDateMillis
                )
            }
        }
    }

    private fun EntityAccount.toDomainAccountInfo(counter: Int): DomainAccountInfo {
        return DomainAccountInfo(
            id = id,
            icon = icon,
            label = label,
            issuer = issuer,
            secret = secret,
            algorithm = algorithm,
            type = type,
            digits = digits.toString(),
            period = period.toString(),
            counter = counter.toString(),
            createdMillis = createDateMillis
        )
    }

    private fun DomainAccountInfo.toEntityAccount(): EntityAccount {
        return EntityAccount(
            id = id,
            icon = icon,
            secret = secret,
            label = label,
            issuer = issuer,
            algorithm = algorithm,
            type = type,
            digits = digits.toInt(),
            period = period.toInt(),
            createDateMillis = createdMillis
        )
    }

}