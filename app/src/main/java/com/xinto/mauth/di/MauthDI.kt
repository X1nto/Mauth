package com.xinto.mauth.di

import androidx.room.Room
import com.xinto.mauth.core.backup.crypto.BackupCrypto
import com.xinto.mauth.core.backup.exporter.AegisExporter
import com.xinto.mauth.core.backup.exporter.FreeOtpPlusExporter
import com.xinto.mauth.core.backup.exporter.MauthExporter
import com.xinto.mauth.core.backup.exporter.UriListExporter
import com.xinto.mauth.core.backup.importer.ImporterRegistry
import com.xinto.mauth.core.backup.importer.AegisImporter
import com.xinto.mauth.core.backup.importer.AndOtpImporter
import com.xinto.mauth.core.backup.importer.BitwardenImporter
import com.xinto.mauth.core.backup.importer.FreeOtpPlusImporter
import com.xinto.mauth.core.backup.importer.MauthImporter
import com.xinto.mauth.core.backup.importer.ProtonImporter
import com.xinto.mauth.core.backup.importer.StratumImporter
import com.xinto.mauth.core.backup.importer.TwoFasImporter
import com.xinto.mauth.core.backup.importer.UriListImporter
import com.xinto.mauth.core.otp.exporter.DefaultOtpExporter
import com.xinto.mauth.core.otp.exporter.OtpExporter
import com.xinto.mauth.core.otp.generator.DefaultOtpGenerator
import com.xinto.mauth.core.otp.generator.OtpGenerator
import com.xinto.mauth.core.otp.parser.DefaultOtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParser
import com.xinto.mauth.core.otp.transformer.DefaultKeyTransformer
import com.xinto.mauth.core.otp.transformer.KeyTransformer
import com.xinto.mauth.db.AccountDatabase
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.QrRepository
import com.xinto.mauth.domain.settings.SettingsRepository
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.backup.BackupRepository
import com.xinto.mauth.domain.group.GroupRepository
import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.ui.screen.account.AccountViewModel
import com.xinto.mauth.ui.screen.auth.AuthViewModel
import com.xinto.mauth.ui.screen.importreview.ImportReviewViewModel
import com.xinto.mauth.ui.screen.importer.ImportViewModel
import com.xinto.mauth.ui.screen.export.AccountExportViewModel
import com.xinto.mauth.ui.screen.export.GoogleAuthenticatorExportViewModel
import com.xinto.mauth.ui.screen.export.ExportViewModel
import com.xinto.mauth.ui.screen.export.FileExportViewModel
import com.xinto.mauth.ui.screen.groups.GroupsViewModel
import com.xinto.mauth.ui.screen.home.HomeViewModel
import com.xinto.mauth.ui.screen.pinremove.PinRemoveViewModel
import com.xinto.mauth.ui.screen.pinsetup.PinSetupViewModel
import com.xinto.mauth.ui.screen.qrscan.QrScanViewModel
import com.xinto.mauth.ui.screen.settings.SettingsViewModel
import com.xinto.mauth.ui.screen.theme.ThemeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object MauthDI {

    val CoreModule = module {
        singleOf(::DefaultOtpGenerator) bind OtpGenerator::class
        singleOf(::DefaultOtpUriParser) bind OtpUriParser::class
        singleOf(::DefaultKeyTransformer) bind KeyTransformer::class
        singleOf(::DefaultOtpExporter) bind OtpExporter::class

        singleOf(::BackupCrypto)
        singleOf(::MauthExporter)
        singleOf(::AegisExporter)
        singleOf(::FreeOtpPlusExporter)
        singleOf(::UriListExporter)

        singleOf(::MauthImporter)
        singleOf(::AegisImporter)
        singleOf(::TwoFasImporter)
        singleOf(::AndOtpImporter)
        singleOf(::BitwardenImporter)
        singleOf(::FreeOtpPlusImporter)
        singleOf(::StratumImporter)
        singleOf(::ProtonImporter)
        singleOf(::UriListImporter)
        single {
            //FIXME: garbage.
            ImporterRegistry(
                importers = listOf(
                    get<MauthImporter>(),
                    get<AegisImporter>(),
                    get<TwoFasImporter>(),
                    get<AndOtpImporter>(),
                    get<BitwardenImporter>(),
                    get<FreeOtpPlusImporter>(),
                    get<StratumImporter>(),
                    get<ProtonImporter>(),
                    get<UriListImporter>(),
                )
            )
        }
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

        single {
            val db: AccountDatabase = get()
            db.groupsDao()
        }
    }

    val DomainModule = module {
        singleOf(::AccountRepository)
        singleOf(::OtpRepository)
        singleOf(::QrRepository)
        singleOf(::SettingsRepository)
        singleOf(::AuthRepository)
        singleOf(::GroupRepository)
        single { BackupRepository(androidContext(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    }

    val UiModule = module {
        viewModelOf(::AccountViewModel)
        viewModelOf(::SettingsViewModel)
        viewModelOf(::QrScanViewModel)
        viewModelOf(::PinSetupViewModel)
        viewModelOf(::PinRemoveViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::AuthViewModel)
        viewModelOf(::ThemeViewModel)
        viewModelOf(::ExportViewModel)
        viewModelOf(::FileExportViewModel)
        viewModelOf(::GoogleAuthenticatorExportViewModel)
        viewModelOf(::AccountExportViewModel)
        viewModelOf(::GroupsViewModel)
        viewModelOf(::ImportViewModel)
        viewModelOf(::ImportReviewViewModel)
    }

    val all = listOf(CoreModule, DbModule, DomainModule, UiModule)

}