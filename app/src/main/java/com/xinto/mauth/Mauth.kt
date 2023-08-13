package com.xinto.mauth

import android.app.Application
import com.xinto.mauth.di.MauthDI
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Mauth : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Mauth)

            modules(MauthDI.all)
        }
    }
}