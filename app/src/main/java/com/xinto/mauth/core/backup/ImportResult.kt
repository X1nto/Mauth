package com.xinto.mauth.core.backup

sealed interface ImportResult {

    data class Success(
        val accounts: List<BackupAccount>,
        val groups: List<BackupGroup> = emptyList(),
        val rejected: List<RejectedEntry> = emptyList()
    ) : ImportResult

    data object NeedsPassword : ImportResult

    data class WrongFormat(val actual: BackupFormat) : ImportResult

    data class Failure(val reason: ImportFailure) : ImportResult
}

data class RejectedEntry(
    val name: String,
    val issuer: String,
    val reason: ImportFailure
)

enum class ImportFailure {
    Unrecognized,
    WrongPassword,
    Malformed,
    UnsupportedVersion,
    InvalidSecret,
    MissingName
}
