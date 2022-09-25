package com.xinto.mauth.di

import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.ui.viewmodel.QrScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val qrScanModule = module {
    fun provideQrScanViewModel(
        otpUriParser: OtpUriParser
    ): QrScannerViewModel {
        return QrScannerViewModel(otpUriParser)
    }

    viewModelOf(::provideQrScanViewModel)
}