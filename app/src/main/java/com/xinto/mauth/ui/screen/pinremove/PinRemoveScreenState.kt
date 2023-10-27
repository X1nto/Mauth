package com.xinto.mauth.ui.screen.pinremove

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PinRemoveScreenState {
    val code: String

    @Immutable
    @JvmInline
    value class Stale(override val code: String) : PinRemoveScreenState

    @Immutable
    data object Error : PinRemoveScreenState {
        override val code: String = ""
    }
}