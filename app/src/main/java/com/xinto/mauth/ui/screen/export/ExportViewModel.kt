package com.xinto.mauth.ui.screen.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.util.catchMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class ExportViewModel(
    private val accounts: List<UUID>,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val state = accountRepository.getAccounts()
        .map { domainAccounts ->
            if (accounts.isEmpty()) return@map domainAccounts

            domainAccounts.filter { accounts.contains(it.id) }
        }
        .map {
            ExportScreenState.Success(it)
        }.catchMap {
            ExportScreenState.Error
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExportScreenState.Loading
        )

}