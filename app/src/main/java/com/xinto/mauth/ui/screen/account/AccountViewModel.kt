package com.xinto.mauth.ui.screen.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.account.usecase.GetAccountInfoUsecase
import com.xinto.mauth.domain.account.usecase.PutAccountUsecase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    getAccountInfoUsecase: GetAccountInfoUsecase,
    private val putAccountUsecase: PutAccountUsecase
) : ViewModel() {

    var state by mutableStateOf<AccountScreenState>(AccountScreenState.Loading)
        private set

    private var fetchJob: Job? = null

    fun saveData(data: DomainAccountInfo) {
        viewModelScope.launch {
            putAccountUsecase(data)
        }
    }

    override fun onCleared() {
        fetchJob?.cancel()
    }

    init {
        when (params) {
            is AccountViewModelParams.Id -> {
                fetchJob = getAccountInfoUsecase(params.id)
                    .catch {
                        state = AccountScreenState.Error(it.localizedMessage ?: it.message ?: it.stackTraceToString())
                    }
                    .onEach {
                        state = AccountScreenState.Success(it)
                    }
                    .launchIn(viewModelScope)
            }
            is AccountViewModelParams.Prefilled -> {
                state = AccountScreenState.Success(params.accountInfo)
            }
        }
    }
}