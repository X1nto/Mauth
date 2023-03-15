package com.xinto.mauth.di.ui

import com.xinto.mauth.ui.screen.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val UiSettingsModule = module {
    viewModelOf(::SettingsViewModel)
}