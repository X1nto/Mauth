package com.xinto.mauth.ui.screen.export

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AccountExportState {

    data object Loading : AccountExportState

    data class Success(
        val label: String,
        val issuer: String,
        val url: String
    ) : AccountExportState

    data object Error : AccountExportState
}
