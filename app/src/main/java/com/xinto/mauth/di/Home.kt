package com.xinto.mauth.di

import android.app.Application
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.domain.repository.HomeRepository
import com.xinto.mauth.domain.repository.HomeRepositoryImpl
import com.xinto.mauth.otp.generator.TotpGenerator
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.transformer.KeyTransformer
import com.xinto.mauth.ui.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val homeModule = module {

    fun provideHomeRepository(accountsDao: AccountsDao): HomeRepository {
        return HomeRepositoryImpl(accountsDao)
    }

    fun provideHomeViewModel(
        application: Application,
        totpGenerator: TotpGenerator,
        otpUriParser: OtpUriParser,
        homeRepository: HomeRepository,
        keyTransformer: KeyTransformer
    ): HomeViewModel {
        return HomeViewModel(application, totpGenerator, keyTransformer, otpUriParser, homeRepository)
    }

    singleOf(::provideHomeRepository)
    viewModelOf(::provideHomeViewModel)
}