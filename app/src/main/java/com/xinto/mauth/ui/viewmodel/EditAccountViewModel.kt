package com.xinto.mauth.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.repository.AddEditAccountRepository
import com.xinto.mauth.ui.screen.AddEditAccountState
import kotlinx.coroutines.launch
import java.util.UUID

class EditAccountViewModel(
    id: UUID,
    application: Application,
    addEditAccountRepository: AddEditAccountRepository
) : AddEditAccountViewModel(application, addEditAccountRepository) {
    init {
        viewModelScope.launch {
            val account = addEditAccountRepository.getAccountInfo(id)
            if (account != null) {
                updateData(account)
                state = AddEditAccountState.Success
            } else {
                state = AddEditAccountState.Error
            }
        }
    }
}