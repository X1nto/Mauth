package com.xinto.mauth.ui.component.lazygroup

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun GroupedListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    type: GroupedItemType = LocalGroupedItemType.current,
    shapes: GroupedItemShapes = GroupedItemDefaults.shapes(),
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = Modifier
            .clip(shapes.shapeForType(type))
            .then(modifier),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}

object GroupedItemDefaults {

    @Composable
    fun shapes(
        onlyShape: CornerBasedShape = MaterialTheme.shapes.medium,
        middleShape: CornerBasedShape = MaterialTheme.shapes.extraSmall,
        firstShape: CornerBasedShape = onlyShape.copy(
            bottomEnd = middleShape.bottomEnd,
            bottomStart = middleShape.bottomStart
        ),
        lastShape: CornerBasedShape = onlyShape.copy(
            topEnd = middleShape.topEnd,
            topStart = middleShape.topStart
        ),
    ): GroupedItemShapes {
        return GroupedItemShapes(
            onlyShape = onlyShape,
            firstShape = firstShape,
            middleShape = middleShape,
            lastShape = lastShape
        )
    }

}

@Immutable
class GroupedItemShapes(
    val onlyShape: Shape,
    val firstShape: Shape,
    val middleShape: Shape,
    val lastShape: Shape
) {

    internal fun shapeForType(type: GroupedItemType): Shape {
        return when (type) {
            GroupedItemType.Only -> onlyShape
            GroupedItemType.First -> firstShape
            GroupedItemType.Middle -> middleShape
            GroupedItemType.Last -> lastShape
        }
    }
}

inline fun LazyListScope.itemsGrouped(
    count: Int,
    noinline key: ((index: Int) -> Any)? = null,
    crossinline contentType: (index: Int) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int) -> Unit
) = items(
    count = count,
    key = key,
    contentType = { contentType(it) },
) {
    val type = remember(it, count) {
        when {
            it == 0 && count == 1 -> GroupedItemType.Only
            it == 0 -> GroupedItemType.First
            it == (count - 1) -> GroupedItemType.Last
            else -> GroupedItemType.Middle
        }
    }
    CompositionLocalProvider(LocalGroupedItemType provides type) {
        itemContent(it)
    }
}

inline fun <T> LazyListScope.itemsGrouped(
    items: Array<T>,
    noinline key: ((item: T) -> Any)? = null,
    crossinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = itemsGrouped(
    count = items.size,
    key = if (key != null) { index: Int -> key(items[index]) } else null,
    contentType = { index -> contentType(items[index]) }
) { i ->
    itemContent(items[i])
}

inline fun <T> LazyListScope.itemsGrouped(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    crossinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = itemsGrouped(
    count = items.size,
    key = if (key != null) { index: Int -> key(items[index]) } else null,
    contentType = { index -> contentType(items[index]) },
) { i ->
    itemContent(items[i])
}