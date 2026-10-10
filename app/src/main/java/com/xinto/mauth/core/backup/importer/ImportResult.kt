package com.xinto.mauth.core.backup.importer

import com.xinto.mauth.core.backup.model.BackupAccount
import com.xinto.mauth.core.backup.model.BackupGroup

sealed interface ImportResult {

    data class Success(
        val source: ImportSource,
        val accounts: List<BackupAccount>,
        val groups: List<BackupGroup> = emptyList(),
        val rejected: List<RejectedEntry> = emptyList()
    ) : ImportResult

    data class NeedsPassword(val source: ImportSource) : ImportResult

    data class WrongFormat(
        val expected: ImportSource,
        val actual: ImportSource
    ) : ImportResult

    data class Failure(val source: ImportSource, val reason: ImportFailure) : ImportResult
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
