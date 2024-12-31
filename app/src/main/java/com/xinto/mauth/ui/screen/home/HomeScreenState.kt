package com.xinto.mauth.ui.screen.home

import androidx.compose.runtime.Immutable
import com.xinto.mauth.domain.account.model.DomainAccount
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface HomeScreenState {

    @Immutable
    data object Loading : HomeScreenState

    @Immutable
    data object Empty : HomeScreenState

    @Immutable
    @JvmInline
    value class Success(val accounts: ImmutableList<DomainAccount>) : HomeScreenState

    @Immutable
    @JvmInline
    value class Error(val error: String) : HomeScreenState

}