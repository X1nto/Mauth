package com.xinto.mauth.ui.component.lazygroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

inline fun LazyListScope.group(
    crossinline header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    item {
        Box(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMedium,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                header()
            }
        }
    }
    content()
    item {
        Spacer(modifier = Modifier.height(16.dp))
    }
}

inline fun LazyListScope.itemGrouped(
    crossinline header: @Composable () -> Unit,
    crossinline itemContent: @Composable LazyItemScope.() -> Unit
) {
    group(header = header) {
        item {
            // The rounded corners of the group come from the per-item ListItemShapes
            // (see ListItemDefaults.segmentedShapes); the column only spaces the segments.
            Column(
                modifier = Modifier.fillParentMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                itemContent()
            }
        }
    }
}
