package com.xinto.mauth.ui.screen.export

import androidx.compose.runtime.Immutable
import com.xinto.mauth.domain.account.model.DomainExportAccount

@Immutable
sealed interface ExportScreenState {

    data object Loading : ExportScreenState

    data class Success(
        val accounts: List<DomainExportAccount>,
        val isAll: Boolean
    ) : ExportScreenState

    data object Empty : ExportScreenState

    data object Error : ExportScreenState
}