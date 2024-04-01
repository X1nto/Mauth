package com.xinto.mauth.ui.component

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * @param actions Aligned respective to the top appbar (3-dot menu last)
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ResponsiveAppBarScaffold(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    appBarTitle: @Composable () -> Unit,
    actions: @Composable RowScope.(arrangement: Arrangement.Horizontal) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End
) {
    val activity = LocalContext.current as Activity
    val sizeClass = calculateWindowSizeClass(activity)
    Scaffold(
        modifier = modifier,
        topBar = {
            if (sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                TopAppBar(
                    title = appBarTitle,
                    actions = {
                        actions(Arrangement.Start)
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                CenterAlignedTopAppBar(
                    title = appBarTitle,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            if (sizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) {
                val currentDirection = LocalLayoutDirection.current
                val newDirection by remember {
                    derivedStateOf {
                        when (currentDirection) {
                            LayoutDirection.Ltr -> LayoutDirection.Rtl
                            LayoutDirection.Rtl -> LayoutDirection.Ltr
                        }
                    }
                }
                BottomAppBar(
                    actions = {
                        actions(Arrangement.Reverse)
                    },
                    floatingActionButton = floatingActionButton
                )
            }
        },
        floatingActionButton = {
            if (sizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                floatingActionButton()
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        content = content
    )
}

private val Arrangement.Reverse
    get() = ReverseHorizontalArrangement

private object ReverseHorizontalArrangement : Arrangement.Horizontal {

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) = if (layoutDirection == LayoutDirection.Ltr) {
        val consumedSize = sizes.fold(0) { a, b -> a + b }
        var current = totalSize - consumedSize
        for (i in (sizes.size - 1) downTo 0) {
            outPositions[i] = current
            current += sizes[i]
        }
    } else {
        var current = 0
        sizes.forEachIndexed { index, it ->
            outPositions[index] = current
            current += it
        }
    }
}