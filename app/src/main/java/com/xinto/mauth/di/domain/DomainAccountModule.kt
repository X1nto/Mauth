package com.xinto.mauth.di.domain

import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.DefaultAccountRepository
import com.xinto.mauth.domain.account.usecase.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DomainAccountModule = module {
    singleOf(::DefaultAccountRepository) bind AccountRepository::class
    singleOf(::GetAccountInfoUsecase)
    singleOf(::GetAccountsUsecase)
    singleOf(::PutAccountUsecase)
    singleOf(::IncrementAccountCounterUsecase)
    singleOf(::DeleteAccountsUsecase)
}