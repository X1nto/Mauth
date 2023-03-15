package com.xinto.mauth.di.domain

import com.xinto.mauth.domain.qr.DefaultQrRepository
import com.xinto.mauth.domain.qr.QrRepository
import com.xinto.mauth.domain.qr.usecase.DecodeQrImageUsecase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DomainQrModule = module {
    singleOf(::DefaultQrRepository) bind QrRepository::class
    singleOf(::DecodeQrImageUsecase)
}