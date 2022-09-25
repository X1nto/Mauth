package com.xinto.mauth.di

import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.ui.viewmodel.QrScanViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val qrScanModule = module {
    fun provideQrScanViewModel(
        otpUriParser: OtpUriParser
    ): QrScanViewModel {
        return QrScanViewModel(otpUriParser)
    }

    viewModelOf(::provideQrScanViewModel)
}