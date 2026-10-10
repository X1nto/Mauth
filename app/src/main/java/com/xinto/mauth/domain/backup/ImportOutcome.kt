package com.xinto.mauth.domain.backup

import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.backup.importer.ImportFailure
import com.xinto.mauth.core.backup.importer.ImportSource
import com.xinto.mauth.domain.backup.model.DomainImportCandidate
import com.xinto.mauth.domain.backup.model.DomainRejectedEntry

@Immutable
sealed interface ImportOutcome {

    data class Parsed(
        val source: ImportSource,
        val candidates: List<DomainImportCandidate>,
        val rejected: List<DomainRejectedEntry>
    ) : ImportOutcome

    data class NeedsPassword(val source: ImportSource) : ImportOutcome

    data class WrongFormat(
        val expected: ImportSource,
        val actual: ImportSource
    ) : ImportOutcome

    data class Failed(val source: ImportSource, val reason: ImportFailure) : ImportOutcome
}
