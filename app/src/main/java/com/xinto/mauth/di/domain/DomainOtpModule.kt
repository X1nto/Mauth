package com.xinto.mauth.di.domain

import com.xinto.mauth.domain.otp.DefaultOtpRepository
import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.domain.otp.usecase.GetOtpRealtimeDataUsecase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DomainOtpModule = module {
    singleOf(::DefaultOtpRepository) bind OtpRepository::class
    singleOf(::GetOtpRealtimeDataUsecase)
}