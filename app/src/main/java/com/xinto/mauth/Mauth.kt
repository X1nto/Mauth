package com.xinto.mauth

import android.app.Application
import com.xinto.mauth.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Mauth : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Mauth)

            modules(homeModule, dbModule, otpModule, addAccountModel, qrScanModule)
        }
    }
}