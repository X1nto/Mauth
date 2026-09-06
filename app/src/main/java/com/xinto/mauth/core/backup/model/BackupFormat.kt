package com.xinto.mauth.core.backup.model

enum class BackupFormat(
    val mimeType: String,
    val extension: String,
    val supportsEncryption: Boolean
) {
    MauthJson("application/json", "json", true),
    AegisVault("application/json", "json", true),
    FreeOtpPlusJson("application/json", "json", false),
    UriList("text/plain", "txt", false),
}
