package com.xinto.mauth.ui.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.AccountRepository
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.util.catchMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AccountViewModelParams {
    @JvmInline
    value class Id(val id: UUID) : AccountViewModelParams

    @JvmInline
    value class Prefilled(val accountInfo: DomainAccountInfo) : AccountViewModelParams
}

class AccountViewModel(
    params: AccountViewModelParams,
    private val accounts: AccountRepository
) : ViewModel() {

    val state = when (params) {
        is AccountViewModelParams.Id -> {
            accounts.getAccountInfo(params.id)
                .map {
                    AccountScreenState.Success(it)
                }.catchMap {
                    AccountScreenState.Error(it.localizedMessage ?: it.message ?: it.stackTraceToString())
                }
        }
        is AccountViewModelParams.Prefilled -> {
            flowOf(AccountScreenState.Success(params.accountInfo))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountScreenState.Loading
    )

    fun saveData(data: DomainAccountInfo) {
        viewModelScope.launch {
            accounts.putAccount(data)
        }
    }
}