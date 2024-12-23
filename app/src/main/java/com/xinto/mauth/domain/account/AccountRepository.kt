package com.xinto.mauth.domain.account

import com.xinto.mauth.core.otp.exporter.OtpExporter
import com.xinto.mauth.core.otp.model.OtpData
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.account.entity.EntityAccount
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData
import com.xinto.mauth.domain.SettingsRepository
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.account.model.DomainExportAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID

class AccountRepository(
    private val accountsDao: AccountsDao,
    private val rtdataDao: RtdataDao,
    private val settingsRepository: SettingsRepository,
    private val otpExporter: OtpExporter
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
        }.flowOn(Dispatchers.IO)
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
        rtdataDao.upsertCountData(EntityCountData(entityAccount.id, domainAccountInfo.counter))
        accountsDao.upsert(entityAccount)
    }

    suspend fun incrementAccountCounter(id: UUID) {
        rtdataDao.incrementAccountCounter(id)
    }

    suspend fun deleteAccounts(ids: List<UUID>) {
        accountsDao.delete(ids.toSet())
    }

    suspend fun DomainAccount.toExportAccount(): DomainExportAccount {
        return DomainExportAccount(
            id = id,
            label = label,
            issuer = issuer,
            icon = icon,
            url = otpExporter.exportOtp(this.toOtpData())
        )
    }

    suspend fun DomainAccount.toOtpData(): OtpData {
        return when (this) {
            is DomainAccount.Hotp -> {
                val counter = rtdataDao.getAccountCounter(id)
                OtpData(
                    label = label,
                    issuer = issuer,
                    secret = secret,
                    algorithm = algorithm,
                    type = OtpType.HOTP,
                    digits = digits,
                    counter = counter,
                    period = null
                )
            }
            is DomainAccount.Totp -> {
                OtpData(
                    label = label,
                    issuer = issuer,
                    secret = secret,
                    algorithm = algorithm,
                    type = OtpType.TOTP,
                    digits = digits,
                    counter = null,
                    period = period
                )
            }
        }
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
            digits = digits,
            period = period,
            counter = counter,
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
            digits = digits,
            period = period,
            createDateMillis = createdMillis
        )
    }

}