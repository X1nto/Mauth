package com.xinto.mauth.di

import com.xinto.mauth.preferences.PreferenceManager
import com.xinto.mauth.preferences.PreferenceManagerImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val preferencesModule = module {
    singleOf(::PreferenceManagerImpl) bind PreferenceManager::class
}