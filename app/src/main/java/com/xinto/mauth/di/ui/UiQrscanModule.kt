package com.xinto.mauth.di.ui

import com.xinto.mauth.ui.screen.qrscan.QrScanViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val UiQrscanModule = module {
    viewModelOf(::QrScanViewModel)
}