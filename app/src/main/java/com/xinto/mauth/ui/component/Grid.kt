package com.xinto.mauth.ui.component

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable

fun LazyGridScope.singleItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable LazyGridItemScope.() -> Unit
) = item(key, span = { GridItemSpan(maxCurrentLineSpan) }, contentType, content)