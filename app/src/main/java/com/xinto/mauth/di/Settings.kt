package com.xinto.mauth.di

import com.xinto.mauth.domain.repository.SettingsRepository
import com.xinto.mauth.domain.repository.SettingsRepositoryImpl
import com.xinto.mauth.ui.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    viewModelOf(::SettingsViewModel)
}