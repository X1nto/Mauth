package com.xinto.mauth.di

import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.ui.viewmodel.AddAccountParams
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val addAccountModel = module {
    fun provideAddAccountViewModel(
        params: AddAccountParams,
        accountsDao: AccountsDao
    ): AddAccountViewModel {
        return AddAccountViewModel(params, accountsDao)
    }

    viewModelOf(::provideAddAccountViewModel)
}