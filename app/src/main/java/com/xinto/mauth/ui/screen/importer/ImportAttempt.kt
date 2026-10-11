package com.xinto.mauth.ui.screen.importer

import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.backup.ImportFailure
import com.xinto.mauth.core.backup.BackupFormat

@Immutable
sealed interface ImportAttempt {

    data object Working : ImportAttempt

    data class AwaitingPassword(
        val source: BackupFormat,
        val wrongPassword: Boolean
    ) : ImportAttempt

    data class WrongFormat(
        val expected: BackupFormat,
        val actual: BackupFormat
    ) : ImportAttempt

    data class Failed(val reason: ImportFailure) : ImportAttempt

    data class NothingToImport(val rejectedCount: Int) : ImportAttempt
}
