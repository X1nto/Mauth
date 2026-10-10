package com.xinto.mauth.core.backup.model

enum class BackupFormat(
    val mimeType: MimeType,
    val supportsEncryption: Boolean
) {
    MauthJson(MimeType.Json, supportsEncryption = true),
    AegisVault(MimeType.Json, supportsEncryption = true),
    FreeOtpPlusJson(MimeType.Json, supportsEncryption = false),
    UriList(MimeType.PlainText, supportsEncryption = false),
}
