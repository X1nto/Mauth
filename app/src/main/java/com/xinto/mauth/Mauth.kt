package com.xinto.mauth

import android.app.Application
import com.xinto.mauth.di.addAccountModel
import com.xinto.mauth.di.dbModule
import com.xinto.mauth.di.homeModule
import com.xinto.mauth.di.otpModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Mauth : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Mauth)

            modules(homeModule, dbModule, otpModule, addAccountModel)
        }
    }
}