package com.xinto.mauth.ui.screen.importer

import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.backup.importer.ImportFailure
import com.xinto.mauth.core.backup.importer.ImportSource

@Immutable
sealed interface ImportAttempt {

    data object Working : ImportAttempt

    data class AwaitingPassword(
        val source: ImportSource,
        val wrongPassword: Boolean
    ) : ImportAttempt

    data class WrongFormat(
        val expected: ImportSource,
        val actual: ImportSource
    ) : ImportAttempt

    data class Failed(val reason: ImportFailure) : ImportAttempt

    data class NothingToImport(val rejectedCount: Int) : ImportAttempt
}
