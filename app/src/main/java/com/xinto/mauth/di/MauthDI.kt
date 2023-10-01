package com.xinto.mauth.di

import androidx.room.Room
import com.xinto.mauth.core.otp.generator.DefaultOtpGenerator
import com.xinto.mauth.core.otp.generator.OtpGenerator
import com.xinto.mauth.core.otp.parser.DefaultOtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParser
import com.xinto.mauth.core.otp.transformer.DefaultKeyTransformer
import com.xinto.mauth.core.otp.transformer.KeyTransformer
import com.xinto.mauth.db.AccountDatabase
import com.xinto.mauth.domain.AccountRepository
import com.xinto.mauth.domain.OtpRepository
import com.xinto.mauth.domain.QrRepository
import com.xinto.mauth.domain.SettingsRepository
import com.xinto.mauth.core.settings.DefaultSettings
import com.xinto.mauth.core.settings.Settings
import com.xinto.mauth.ui.screen.account.AccountViewModel
import com.xinto.mauth.ui.screen.auth.AuthViewModel
import com.xinto.mauth.ui.screen.home.HomeViewModel
import com.xinto.mauth.ui.screen.pinsetup.PinSetupViewModel
import com.xinto.mauth.ui.screen.qrscan.QrScanViewModel
import com.xinto.mauth.ui.screen.settings.SettingsViewModel
import org.apache.commons.codec.binary.Base32
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object MauthDI {

    val CoreModule = module {
        singleOf(::DefaultOtpGenerator) bind OtpGenerator::class
        singleOf(::DefaultOtpUriParser) bind OtpUriParser::class
        single {
            DefaultKeyTransformer(Base32())
        } bind KeyTransformer::class
        singleOf(::DefaultSettings) bind Settings::class
    }

    val DbModule = module {
        single {
            Room.databaseBuilder(androidContext(), AccountDatabase::class.java, "accounts")
                .addMigrations(AccountDatabase.Migrate3to4)
                .addMigrations(AccountDatabase.Migrate4To5)
                .build()
        }

        single {
            val db: AccountDatabase = get()
            db.accountsDao()
        }

        single {
            val db: AccountDatabase = get()
            db.rtdataDao()
        }
    }

    val DomainModule = module {
        singleOf(::AccountRepository)
        singleOf(::OtpRepository)
        singleOf(::QrRepository)
        singleOf(::SettingsRepository)
    }

    val UiModule = module {
        viewModelOf(::AccountViewModel)
        viewModelOf(::SettingsViewModel)
        viewModelOf(::QrScanViewModel)
        viewModelOf(::PinSetupViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::AuthViewModel)
    }

    val all = listOf(CoreModule, DbModule, DomainModule, UiModule)

}