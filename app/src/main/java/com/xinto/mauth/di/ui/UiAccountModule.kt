package com.xinto.mauth.di.ui

import com.xinto.mauth.ui.screen.account.AccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val UiAccountModule = module {
    viewModelOf(::AccountViewModel)
}