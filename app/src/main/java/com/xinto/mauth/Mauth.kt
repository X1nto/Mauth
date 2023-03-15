package com.xinto.mauth

import android.app.Application
import com.xinto.mauth.di.core.CoreOtpModule
import com.xinto.mauth.di.db.DbAccountModule
import com.xinto.mauth.di.db.DbModule
import com.xinto.mauth.di.db.DbRtdataModule
import com.xinto.mauth.di.domain.DomainAccountModule
import com.xinto.mauth.di.domain.DomainOtpModule
import com.xinto.mauth.di.domain.DomainQrModule
import com.xinto.mauth.di.domain.DomainSettingsModule
import com.xinto.mauth.di.ui.UiAccountModule
import com.xinto.mauth.di.ui.UiHomeModule
import com.xinto.mauth.di.ui.UiQrscanModule
import com.xinto.mauth.di.ui.UiSettingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Mauth : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Mauth)

            modules(
                CoreOtpModule,

                DbModule,
                DbAccountModule,
                DbRtdataModule,

                DomainAccountModule,
                DomainOtpModule,
                DomainSettingsModule,
                DomainQrModule,

                UiAccountModule,
                UiHomeModule,
                UiSettingsModule,
                UiQrscanModule,
            )
        }
    }
}