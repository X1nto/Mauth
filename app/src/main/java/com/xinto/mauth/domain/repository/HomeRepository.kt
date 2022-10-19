package com.xinto.mauth.domain.repository

import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.otp.OtpType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

interface HomeRepository {

    suspend fun getAccounts(): List<DomainAccount>

    suspend fun observeAccounts(): Flow<List<DomainAccount>>

    suspend fun deleteAccounts(accounts: List<UUID>)

    suspend fun incrementAccountCounter(id: UUID)

}

class HomeRepositoryImpl(
    private val accountsDao: AccountsDao
) : HomeRepository {

    override suspend fun getAccounts(): List<DomainAccount> {
        return accountsDao.getAll().map {
            it.toDomain()
        }
    }

    override suspend fun observeAccounts(): Flow<List<DomainAccount>> {
        return accountsDao.observeAll().map { entityAccounts ->
            entityAccounts.map {
                it.toDomain()
            }
        }
    }

    override suspend fun deleteAccounts(accounts: List<UUID>) {
        accountsDao.delete(accounts)
    }

    override suspend fun incrementAccountCounter(id: UUID) {
        val account = accountsDao.getById(id) ?: return
        accountsDao.update(
            account.copy(counter = account.counter + 1)
        )
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
                    counter = counter
                )
            }
        }
    }

}