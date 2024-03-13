package com.xinto.mauth.ui.component.lazygroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

inline fun LazyListScope.group(
    crossinline header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    item {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.headlineMedium) {
            header()
        }
    }
    item {
        Spacer(modifier = Modifier.height(8.dp))
    }
    content()
    item {
        Spacer(modifier = Modifier.height(12.dp))
    }
}

inline fun LazyListScope.itemGrouped(
    crossinline header: @Composable () -> Unit,
    crossinline itemContent: @Composable LazyItemScope.() -> Unit
) {
    group(header = header) {
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CompositionLocalProvider(LocalGroupedItemType provides GroupedItemType.Middle) {
                    itemContent()
                }
            }
        }
    }
}

inline fun LazyListScope.itemsGrouped(
    crossinline header: @Composable () -> Unit,
    count: Int,
    noinline key: ((index: Int) -> Any)? = null,
    crossinline contentType: (index: Int) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int) -> Unit
) {
    group(header = header) {
        itemsGrouped(
            count = count,
            key = key,
            contentType = contentType,
            itemContent = itemContent
        )
    }
}

inline fun <T> LazyListScope.itemsGrouped(
    crossinline header: @Composable () -> Unit,
    items: Array<T>,
    noinline key: ((item: T) -> Any)? = null,
    crossinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    group(header = header) {
        itemsGrouped(
            items = items,
            key = key,
            contentType = contentType,
            itemContent = itemContent
        )
    }
}

inline fun <T> LazyListScope.itemsGrouped(
    crossinline header: @Composable () -> Unit,
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    crossinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    group(header = header) {
        itemsGrouped(
            items = items,
            key = key,
            contentType = contentType,
            itemContent = itemContent
        )
    }
}