package com.xinto.mauth.ui.screen.importer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.importer.ImportFailure
import com.xinto.mauth.core.backup.importer.ImportSource
import com.xinto.mauth.core.backup.model.labels

@get:StringRes
val ImportSource.titleRes: Int
    get() = when (this) {
        ImportSource.Mauth -> R.string.import_source_mauth
        ImportSource.UriList -> R.string.import_source_urilist
        ImportSource.Aegis -> R.string.import_source_aegis
        ImportSource.TwoFas -> R.string.import_source_2fas
        ImportSource.AndOtp -> R.string.import_source_andotp
        ImportSource.Bitwarden -> R.string.import_source_bitwarden
        ImportSource.FreeOtpPlus -> R.string.import_source_freeotpplus
        ImportSource.Stratum -> R.string.import_source_stratum
        ImportSource.Proton -> R.string.import_source_proton
        ImportSource.Unknown -> R.string.import_source_unknown
    }

val ImportSource.format: String?
    get() = mimeTypes.labels()

@get:StringRes
val ImportSource.encryptionRes: Int?
    get() = when (this) {
        ImportSource.Mauth,
        ImportSource.Aegis,
        ImportSource.TwoFas,
        ImportSource.AndOtp,
        ImportSource.Bitwarden,
        ImportSource.Proton -> R.string.import_encryption_both

        ImportSource.Stratum -> R.string.import_encryption_unencrypted

        ImportSource.FreeOtpPlus,
        ImportSource.UriList,
        ImportSource.Unknown -> null
    }

sealed interface SourceIcon {
    data class AppIcon(@DrawableRes val res: Int) : SourceIcon
    data class Glyph(@DrawableRes val res: Int) : SourceIcon
}

val ImportSource.icon: SourceIcon
    get() = when (this) {
        ImportSource.Mauth -> SourceIcon.AppIcon(R.mipmap.ic_launcher)
        ImportSource.Aegis -> SourceIcon.AppIcon(R.drawable.ic_aegis)
        ImportSource.TwoFas -> SourceIcon.AppIcon(R.drawable.ic_2fas)
        ImportSource.AndOtp -> SourceIcon.AppIcon(R.drawable.ic_andotp)
        ImportSource.Bitwarden -> SourceIcon.AppIcon(R.drawable.ic_bitwarden)
        ImportSource.FreeOtpPlus -> SourceIcon.AppIcon(R.drawable.ic_freeotpplus)
        ImportSource.Stratum -> SourceIcon.AppIcon(R.drawable.ic_stratum)
        ImportSource.Proton -> SourceIcon.AppIcon(R.drawable.ic_proton)
        ImportSource.UriList -> SourceIcon.Glyph(R.drawable.ic_link)
        ImportSource.Unknown -> SourceIcon.Glyph(0)
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
