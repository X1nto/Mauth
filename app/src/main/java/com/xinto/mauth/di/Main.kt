package com.xinto.mauth.di

import com.xinto.mauth.domain.repository.MainRepository
import com.xinto.mauth.domain.repository.MainRepositoryImpl
import com.xinto.mauth.ui.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    singleOf(::MainRepositoryImpl) bind MainRepository::class
    viewModelOf(::MainViewModel)
}