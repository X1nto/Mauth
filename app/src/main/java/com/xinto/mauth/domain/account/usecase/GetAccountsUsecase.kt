package com.xinto.mauth.domain.account.usecase

import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUsecase(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<List<DomainAccount>> {
        return repository.getAccounts()
    }
}