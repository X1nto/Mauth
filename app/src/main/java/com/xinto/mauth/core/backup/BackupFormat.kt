package com.xinto.mauth.core.backup

enum class BackupFormat(val mimeTypes: List<MimeType>) {
    Mauth(listOf(MimeType.Json)),
    Aegis(listOf(MimeType.Json)),
    TwoFas(listOf(MimeType.Json, MimeType.Binary)),
    AndOtp(listOf(MimeType.Json, MimeType.Binary)),
    Bitwarden(listOf(MimeType.Json, MimeType.Csv)),
    FreeOtpPlus(listOf(MimeType.Json)),
    Stratum(listOf(MimeType.Json, MimeType.Binary)),
    Proton(listOf(MimeType.Json)),

    UriList(listOf(MimeType.PlainText));

    companion object {

        val appFormats = entries.filter { it != UriList }

        val otherFormats = listOf(UriList)
    }
}

interface BackupImporter {

    fun sniff(content: String): Boolean

    fun import(content: String, password: CharArray?): ImportResult
}

interface BackupExporter {

    val mimeType: MimeType

    val supportsEncryption: Boolean

    fun export(contents: BackupData, password: CharArray?): String

}

val BackupFormat.importer: BackupImporter
    get() = when (this) {
        BackupFormat.Mauth -> MauthBackup
        BackupFormat.Aegis -> AegisBackup
        BackupFormat.TwoFas -> TwoFasBackup
        BackupFormat.AndOtp -> AndOtpBackup
        BackupFormat.Bitwarden -> BitwardenBackup
        BackupFormat.FreeOtpPlus -> FreeOtpPlusBackup
        BackupFormat.Stratum -> StratumBackup
        BackupFormat.Proton -> ProtonBackup
        BackupFormat.UriList -> UriListBackup
    }

val BackupFormat.exporter: BackupExporter?
    get() = when (this) {
        BackupFormat.Mauth -> MauthBackup
        BackupFormat.Aegis -> AegisBackup
        BackupFormat.FreeOtpPlus -> FreeOtpPlusBackup
        BackupFormat.UriList -> UriListBackup

        BackupFormat.TwoFas,
        BackupFormat.AndOtp,
        BackupFormat.Bitwarden,
        BackupFormat.Stratum,
        BackupFormat.Proton -> null
    }

fun detectBackupFormat(content: String): BackupFormat? {
    return BackupFormat.entries.firstOrNull {
        try {
            it.importer.sniff(content)
        } catch (_: Exception) {
            false
        }
    }
}

fun BackupFormat.import(content: String, password: CharArray?): ImportResult {
    val result = try {
        importer.import(content, password)
    } catch (_: Exception) {
        ImportResult.Failure(ImportFailure.Malformed)
    }

    if (result !is ImportResult.Failure || result.reason == ImportFailure.WrongPassword) {
        return result
    }

    val detected = detectBackupFormat(content)
    if (detected != null && detected != this) {
        return ImportResult.WrongFormat(actual = detected)
    }

    return result
}
