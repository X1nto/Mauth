package com.xinto.mauth.di.domain

import com.xinto.mauth.domain.settings.DefaultSettingsRepository
import com.xinto.mauth.domain.settings.SettingsRepository
import com.xinto.mauth.domain.settings.usecase.GetSecureModeUsecase
import com.xinto.mauth.domain.settings.usecase.GetSortModeUsecase
import com.xinto.mauth.domain.settings.usecase.SetSecureModeUsecase
import com.xinto.mauth.domain.settings.usecase.SetSortModeUsecase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DomainSettingsModule = module {
    singleOf(::DefaultSettingsRepository) bind SettingsRepository::class
    singleOf(::GetSecureModeUsecase)
    singleOf(::SetSecureModeUsecase)
    singleOf(::GetSortModeUsecase)
    singleOf(::SetSortModeUsecase)
}