package com.xinto.mauth.di

import android.app.Application
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.ui.navigation.AddAccountParams
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val addAccountModel = module {
    fun provideAddAccountViewModel(
        application: Application,
        params: AddAccountParams,
        accountsDao: AccountsDao
    ): AddAccountViewModel {
        return AddAccountViewModel(application, params, accountsDao)
    }

    viewModelOf(::provideAddAccountViewModel)
}