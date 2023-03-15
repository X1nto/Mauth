package com.xinto.mauth.domain.account.usecase

import com.xinto.mauth.domain.account.AccountRepository
import java.util.UUID

class DeleteAccountsUsecase(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(ids: List<UUID>) {
        repository.deleteAccounts(ids)
    }

}