package com.xinto.mauth.domain.account

import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import kotlinx.coroutines.flow.Flow
import java.util.*

interface AccountRepository {

    fun getAccounts(): Flow<List<DomainAccount>>
    fun getAccountInfo(id: UUID): Flow<DomainAccountInfo>

    suspend fun putAccount(domainAccountInfo: DomainAccountInfo)
    suspend fun incrementAccountCounter(id: UUID)
    suspend fun deleteAccounts(ids: List<UUID>)

}