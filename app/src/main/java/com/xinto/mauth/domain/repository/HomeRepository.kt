package com.xinto.mauth.domain.repository

import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.otp.OtpType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface HomeRepository {

    suspend fun getAccounts(): List<DomainAccount>

    suspend fun observeAccounts(): Flow<List<DomainAccount>>

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

    private fun EntityAccount.toDomain(): DomainAccount {
        val idString = id.toString()
        return when (type) {
            OtpType.Totp -> {
                DomainAccount.Totp(
                    id = idString,
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
                    id = idString,
                    secret = secret,
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