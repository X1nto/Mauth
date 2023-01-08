package com.xinto.mauth.di

import com.xinto.mauth.domain.repository.HomeRepository
import com.xinto.mauth.domain.repository.HomeRepositoryImpl
import com.xinto.mauth.ui.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val homeModule = module {
    singleOf(::HomeRepositoryImpl) bind HomeRepository::class
    viewModelOf(::HomeViewModel)
}