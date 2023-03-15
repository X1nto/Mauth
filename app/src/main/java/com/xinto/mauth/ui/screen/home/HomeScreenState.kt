package com.xinto.mauth.ui.screen.home

import androidx.compose.runtime.Immutable
import com.xinto.mauth.domain.account.model.DomainAccount

@Immutable
sealed interface HomeScreenState {

    @Immutable
    object Loading : HomeScreenState

    @Immutable
    object Empty : HomeScreenState

    @Immutable
    @JvmInline
    value class Success(val accounts: List<DomainAccount>) : HomeScreenState

    @Immutable
    @JvmInline
    value class Error(val error: String) : HomeScreenState

}