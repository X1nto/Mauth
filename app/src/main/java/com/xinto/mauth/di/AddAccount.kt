package com.xinto.mauth.di

import android.app.Application
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.ui.viewmodel.AddEditAccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val addAccountModel = module {
    fun provideAddAccountViewModel(
        application: Application,
        accountsDao: AccountsDao
    ): AddEditAccountViewModel {
        return AddEditAccountViewModel(application, accountsDao)
    }

    viewModelOf(::provideAddAccountViewModel)
}