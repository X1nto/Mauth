package com.xinto.mauth.domain.account.usecase

import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.account.AccountRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GetAccountInfoUsecase(
    private val repository: AccountRepository
) {

    operator fun invoke(id: UUID): Flow<DomainAccountInfo> {
        return repository.getAccountInfo(id)
    }

}