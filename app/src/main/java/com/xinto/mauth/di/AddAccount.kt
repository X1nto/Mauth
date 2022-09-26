package com.xinto.mauth.di

import android.app.Application
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val addAccountModel = module {
    fun provideAddAccountViewModel(
        application: Application,
        accountsDao: AccountsDao
    ): AddAccountViewModel {
        return AddAccountViewModel(application, accountsDao)
    }

    viewModelOf(::provideAddAccountViewModel)
}