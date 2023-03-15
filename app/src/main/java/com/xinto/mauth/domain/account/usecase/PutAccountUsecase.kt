package com.xinto.mauth.domain.account.usecase

import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.account.AccountRepository

class PutAccountUsecase(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(accountInfo: DomainAccountInfo) {
        return repository.putAccount(accountInfo)
    }

}