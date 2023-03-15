package com.xinto.mauth.domain.account.usecase

import com.xinto.mauth.domain.account.AccountRepository
import java.util.UUID

class IncrementAccountCounterUsecase(
    private val repository: AccountRepository
) {

    suspend operator fun invoke(id: UUID) {
        repository.incrementAccountCounter(id)
    }

}