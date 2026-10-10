package com.xinto.mauth.ui.screen.importreview

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ImportReviewState {

    @Immutable
    data object Reviewing : ImportReviewState

    @Immutable
    data object Importing : ImportReviewState

    @Immutable
    data object Failed : ImportReviewState

    @Immutable
    @JvmInline
    value class Imported(val count: Int) : ImportReviewState
}
