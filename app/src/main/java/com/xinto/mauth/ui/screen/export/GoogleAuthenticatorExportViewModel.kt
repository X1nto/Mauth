package com.xinto.mauth.ui.screen.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.group.model.GroupFilter
import com.xinto.mauth.util.catchMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class GoogleAuthenticatorExportViewModel(
    private val accounts: List<UUID>,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    val state = accountRepository.getAccounts(GroupFilter.All)
        .map { all ->
            val filtered = if (accounts.isEmpty()) {
                all
            } else {
                all.filter { accounts.contains(it.id) }
            }

            if (filtered.isEmpty()) {
                return@map GoogleAuthenticatorExportState.Empty
            }

            GoogleAuthenticatorExportState.Success(
                uris = with(accountRepository) {
                    filtered.toBatchOtpUrl()
                }
            )
        }
        .catchMap { GoogleAuthenticatorExportState.Error }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoogleAuthenticatorExportState.Loading
        )
}
