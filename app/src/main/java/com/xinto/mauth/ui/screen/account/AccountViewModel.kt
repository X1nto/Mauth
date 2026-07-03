package com.xinto.mauth.ui.screen.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.group.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    application: Application,

    params: AccountViewModelParams,
    private val accounts: AccountRepository,
    private val groupRepository: GroupRepository
) : AndroidViewModel(application) {

    private val _initialInfo = MutableStateFlow<DomainAccountInfo?>(null)

    private val _state = MutableStateFlow<AccountScreenState>(AccountScreenState.Loading)
    val state = _state.asStateFlow()

    private val groups = groupRepository.getGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        when (params) {
            is AccountViewModelParams.Id -> {
                accounts.getAccountInfo(params.id)
                    .onEach {
                        _initialInfo.value = it
                        _state.value = AccountScreenState.Success(AccountForm(it, groups, groupRepository::createGroup))
                    }.catch {
                        _state.value = AccountScreenState.Error(it.localizedMessage ?: it.message ?: it.stackTraceToString())
                    }.launchIn(viewModelScope)
            }
            is AccountViewModelParams.Prefilled -> {
                _initialInfo.value = params.accountInfo
                _state.value = AccountScreenState.Success(AccountForm(params.accountInfo, groups, groupRepository::createGroup))
            }
        }
    }

    fun saveData(accountInfo: DomainAccountInfo) {
        viewModelScope.launch {
            accounts.putAccount(accountInfo)
        }
    }
}