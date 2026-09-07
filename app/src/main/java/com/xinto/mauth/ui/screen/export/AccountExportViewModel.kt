package com.xinto.mauth.ui.screen.export

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.group.model.GroupFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class AccountExportViewModel(
    application: Application,

    private val id: UUID,
    private val accountRepository: AccountRepository,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<AccountExportState>(AccountExportState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val account = accountRepository.getAccounts(GroupFilter.All)
                    .first()
                    .first { it.id == id }

                _state.value = AccountExportState.Success(
                    label = account.label,
                    issuer = account.issuer,
                    url = with(accountRepository) { account.toOtpUrl() }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = AccountExportState.Error
            }
        }
    }
}
