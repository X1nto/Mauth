package com.xinto.mauth.domain.backup

import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.backup.ImportFailure
import com.xinto.mauth.core.backup.BackupFormat
import com.xinto.mauth.domain.backup.model.DomainImportCandidate
import com.xinto.mauth.domain.backup.model.DomainRejectedEntry

@Immutable
sealed interface ImportOutcome {

    data class Parsed(
        val source: BackupFormat?,
        val candidates: List<DomainImportCandidate>,
        val rejected: List<DomainRejectedEntry>
    ) : ImportOutcome

    data class NeedsPassword(val source: BackupFormat) : ImportOutcome

    data class WrongFormat(
        val expected: BackupFormat,
        val actual: BackupFormat
    ) : ImportOutcome

    data class Failed(val source: BackupFormat, val reason: ImportFailure) : ImportOutcome
}
