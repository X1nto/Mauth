package com.xinto.mauth.ui.screen.account

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AccountScreenState {

    @Immutable
    data object Loading : AccountScreenState

    @Immutable
    data class Success(val form: AccountForm) : AccountScreenState

    @Immutable
    data class Error(val error: String) : AccountScreenState

}