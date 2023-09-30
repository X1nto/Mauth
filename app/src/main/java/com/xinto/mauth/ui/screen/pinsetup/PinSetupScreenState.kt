package com.xinto.mauth.ui.screen.pinsetup

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PinSetupScreenState {
    @Immutable
    data object Initial : PinSetupScreenState

    @Immutable
    data object Confirm : PinSetupScreenState
}