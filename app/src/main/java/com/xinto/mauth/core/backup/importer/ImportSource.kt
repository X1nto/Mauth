package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.model.MimeType

enum class ImportSource(val mimeTypes: List<MimeType>) {
    Mauth(listOf(MimeType.Json)),
    Aegis(listOf(MimeType.Json)),
    TwoFas(listOf(MimeType.Json, MimeType.Binary)),
    AndOtp(listOf(MimeType.Json, MimeType.Binary)),
    Bitwarden(listOf(MimeType.Json, MimeType.Csv)),
    FreeOtpPlus(listOf(MimeType.Json)),
    Stratum(listOf(MimeType.Json, MimeType.Binary)),
    Proton(listOf(MimeType.Json)),

    UriList(listOf(MimeType.PlainText)),

    Unknown(emptyList());

    companion object {

        val appSources = entries.filter { it != UriList && it != Unknown }

        val otherSources = listOf(UriList)
    }
}
