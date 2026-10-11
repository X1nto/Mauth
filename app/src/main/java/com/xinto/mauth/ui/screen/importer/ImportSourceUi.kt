package com.xinto.mauth.ui.screen.importer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.ImportFailure
import com.xinto.mauth.core.backup.BackupFormat
import com.xinto.mauth.core.backup.labels

@get:StringRes
val BackupFormat.titleRes: Int
    get() = when (this) {
        BackupFormat.Mauth -> R.string.app_name
        BackupFormat.UriList -> R.string.import_source_urilist
        BackupFormat.Aegis -> R.string.backup_app_aegis
        BackupFormat.TwoFas -> R.string.backup_app_2fas
        BackupFormat.AndOtp -> R.string.backup_app_andotp
        BackupFormat.Bitwarden -> R.string.backup_app_bitwarden
        BackupFormat.FreeOtpPlus -> R.string.backup_app_freeotpplus
        BackupFormat.Stratum -> R.string.backup_app_stratum
        BackupFormat.Proton -> R.string.backup_app_proton
    }

val BackupFormat.format: String?
    get() = mimeTypes.labels()

@get:StringRes
val BackupFormat.encryptionRes: Int?
    get() = when (this) {
        BackupFormat.Mauth,
        BackupFormat.Aegis,
        BackupFormat.TwoFas,
        BackupFormat.AndOtp,
        BackupFormat.Bitwarden,
        BackupFormat.Proton -> R.string.backup_encryption_both

        BackupFormat.Stratum -> R.string.backup_encryption_unencrypted

        BackupFormat.FreeOtpPlus,
        BackupFormat.UriList -> null
    }

sealed interface SourceIcon {
    data class AppIcon(@DrawableRes val res: Int) : SourceIcon
    data class Glyph(@DrawableRes val res: Int) : SourceIcon
}

val BackupFormat.icon: SourceIcon
    get() = when (this) {
        BackupFormat.Mauth -> SourceIcon.AppIcon(R.mipmap.ic_launcher)
        BackupFormat.Aegis -> SourceIcon.AppIcon(R.drawable.ic_aegis)
        BackupFormat.TwoFas -> SourceIcon.AppIcon(R.drawable.ic_2fas)
        BackupFormat.AndOtp -> SourceIcon.AppIcon(R.drawable.ic_andotp)
        BackupFormat.Bitwarden -> SourceIcon.AppIcon(R.drawable.ic_bitwarden)
        BackupFormat.FreeOtpPlus -> SourceIcon.AppIcon(R.drawable.ic_freeotpplus)
        BackupFormat.Stratum -> SourceIcon.AppIcon(R.drawable.ic_stratum)
        BackupFormat.Proton -> SourceIcon.AppIcon(R.drawable.ic_proton)
        BackupFormat.UriList -> SourceIcon.Glyph(R.drawable.ic_link)
    }

@get:StringRes
val ImportFailure.rejectionReasonRes: Int
    get() = when (this) {
        ImportFailure.InvalidSecret -> R.string.import_rejected_reason_invalidsecret
        ImportFailure.Malformed -> R.string.import_rejected_reason_malformed
        ImportFailure.Unrecognized -> R.string.import_rejected_reason_unsupported
        ImportFailure.MissingName -> R.string.import_rejected_reason_missingname
        ImportFailure.WrongPassword -> R.string.import_failure_wrongpassword
        ImportFailure.UnsupportedVersion -> R.string.import_failure_unsupportedversion
    }
