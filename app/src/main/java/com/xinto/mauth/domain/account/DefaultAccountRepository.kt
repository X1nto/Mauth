package com.xinto.mauth.domain.account

import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.account.entity.EntityAccount
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.NoSuchElementException

class DefaultAccountRepository(
    private val accountsDao: AccountsDao,
    private val rtdataDao: RtdataDao
) : AccountRepository {

    override fun getAccounts(): Flow<List<DomainAccount>> {
        return accountsDao.observeAll()
            .map { entityAccounts ->
                entityAccounts.map { it.toDomain() }
            }
    }

    override fun getAccountInfo(id: UUID): Flow<DomainAccountInfo> {
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

    override suspend fun putAccount(domainAccountInfo: DomainAccountInfo) {
        val entityAccount = domainAccountInfo.toEntityAccount()
        rtdataDao.upsertCountData(EntityCountData(entityAccount.id, domainAccountInfo.counter.toInt()))
        accountsDao.upsert(entityAccount)
    }

    override suspend fun incrementAccountCounter(id: UUID) {
        rtdataDao.incrementAccountCounter(id)
    }

    override suspend fun deleteAccounts(ids: List<UUID>) {
        accountsDao.delete(ids.toSet())
    }

    private fun EntityAccount.toDomain(): DomainAccount {
        return when (type) {
            OtpType.Totp -> {
                DomainAccount.Totp(
                    id = id,
                    icon = icon,
                    secret = secret,
                    label = label,
                    issuer = issuer,
                    algorithm = algorithm,
                    digits = digits,
                    period = period
                )
            }
            OtpType.Hotp -> {
                DomainAccount.Hotp(
                    id = id,
                    secret = secret,
                    icon = icon,
                    label = label,
                    issuer = issuer,
                    algorithm = algorithm,
                    digits = digits,
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
            counter = counter.toString()
        )
    }

    private fun DomainAccountInfo.toEntityAccount(): EntityAccount {
        return EntityAccount(
            id = id ?: UUID.randomUUID(),
            icon = icon,
            secret = secret,
            label = label,
            issuer = issuer,
            algorithm = algorithm,
            type = type,
            digits = digits.toInt(),
            period = period.toInt(),
            createDateMillis = 0
        )
    }

}