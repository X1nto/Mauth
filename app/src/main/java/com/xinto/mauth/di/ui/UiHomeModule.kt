package com.xinto.mauth.di.ui

import com.xinto.mauth.ui.screen.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val UiHomeModule = module {
    viewModelOf(::HomeViewModel)
}