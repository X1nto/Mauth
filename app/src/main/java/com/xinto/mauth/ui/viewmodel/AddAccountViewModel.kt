package com.xinto.mauth.ui.viewmodel

import android.app.Application
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.domain.repository.AddEditAccountRepository
import com.xinto.mauth.ui.screen.AddEditAccountState

class AddAccountViewModel(
    accountInfo: DomainAccountInfo,
    application: Application,
    addEditAccountRepository: AddEditAccountRepository
) : AddEditAccountViewModel(application, addEditAccountRepository) {
    init {
        updateData(accountInfo)
        state = AddEditAccountState.Success
    }
}