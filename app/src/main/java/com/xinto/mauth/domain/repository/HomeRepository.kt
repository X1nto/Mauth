package com.xinto.mauth.domain.repository

import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.domain.model.DomainAccount
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
            DomainAccount(
                id = it.id,
                label = it.label,
                secret = it.secret
            )
        }
    }

    override suspend fun observeAccounts(): Flow<List<DomainAccount>> {
        return accountsDao.observeAll().map { entityAccounts ->
            entityAccounts.map {
                DomainAccount(
                    id = it.id,
                    label = it.label,
                    secret = it.secret
                )
            }
        }
    }

}