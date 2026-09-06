package com.xinto.mauth.ui.screen.export

import androidx.compose.runtime.Immutable

@Immutable
sealed interface GoogleAuthenticatorExportState {

    data object Loading : GoogleAuthenticatorExportState

    data class Success(val uris: List<String>) : GoogleAuthenticatorExportState

    data object Empty : GoogleAuthenticatorExportState

    data object Error : GoogleAuthenticatorExportState
}
