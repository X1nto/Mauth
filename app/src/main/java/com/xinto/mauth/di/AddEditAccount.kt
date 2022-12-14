package com.xinto.mauth.di

import com.xinto.mauth.domain.repository.AddEditAccountRepository
import com.xinto.mauth.domain.repository.AddEditAccountRepositoryImpl
import com.xinto.mauth.ui.viewmodel.AddAccountViewModel
import com.xinto.mauth.ui.viewmodel.EditAccountViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val addEditAccountModule = module {
    singleOf(::AddEditAccountRepositoryImpl) bind AddEditAccountRepository::class
    viewModelOf(::AddAccountViewModel)
    viewModelOf(::EditAccountViewModel)
}