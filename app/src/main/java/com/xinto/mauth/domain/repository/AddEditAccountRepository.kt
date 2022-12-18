package com.xinto.mauth.domain.repository

import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount
import com.xinto.mauth.domain.model.DomainAccountInfo
import java.util.UUID

interface AddEditAccountRepository {

    suspend fun getAccountInfo(id: UUID): DomainAccountInfo?

    suspend fun saveAccount(info: DomainAccountInfo)

}

class AddEditAccountRepositoryImpl(
    private val accountsDao: AccountsDao
) : AddEditAccountRepository {

    override suspend fun getAccountInfo(id: UUID): DomainAccountInfo? {
        return accountsDao.getById(id)?.toDomainAccountInfo()
    }

    override suspend fun saveAccount(info: DomainAccountInfo) {
        val entityAccount = info.toEntityAccount()
        if (info.id == null) {
            accountsDao.insert(entityAccount)
        } else {
            accountsDao.update(entityAccount)
        }
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
            digits = digits,
            counter = counter,
            period = period
        )
    }

    private fun EntityAccount.toDomainAccountInfo(): DomainAccountInfo {
        return DomainAccountInfo(
            id = id,
            icon = icon,
            label = label,
            issuer = issuer,
            secret = secret,
            algorithm = algorithm,
            type = type,
            digits = digits,
            counter = counter,
            period = period
        )
    }

}