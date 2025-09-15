package com.xinto.mauth.ui.screen.qrscan

import androidx.compose.runtime.Immutable

@Immutable
data class BatchData(
    val current: Int,
    val outOf: Int
)