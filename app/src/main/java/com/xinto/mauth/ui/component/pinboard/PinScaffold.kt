package com.xinto.mauth.ui.component.pinboard

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.xinto.mauth.ui.theme.MauthTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PinScaffold(
    modifier: Modifier = Modifier,
    state: PinBoardState = rememberPinBoardState(),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    windowSizeClass: WindowSizeClass = calculateWindowSizeClass(LocalActivity.current!!),
    description: (@Composable () -> Unit)? = null,
    error: Boolean = false,
    codeLength: Int,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) {
        if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (description != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.headlineMedium.copy(
                                    textAlign = TextAlign.Center
                                )
                            ) {
                                description()
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    PinDisplay(
                        modifier = Modifier
                            .fillMaxWidth(0.5f),
                        length = codeLength,
                        error = error,
                    )
                }
                PinBoard(
                    modifier = Modifier.widthIn(max = 250.dp),
                    horizontalButtonSpace = 16.dp,
                    minButtonSize = PinButtonDefaults.PinButtonSmallMinSize,
                    state = state
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(start = 40.dp, top = 40.dp, end = 40.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) 400.dp else Dp.Unspecified),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (description != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.headlineMedium.copy(
                                    textAlign = TextAlign.Center
                                )
                            ) {
                                description()
                            }
                        }
                    }
                    PinDisplay(
                        modifier = Modifier
                            .fillMaxWidth(),
                        length = codeLength,
                        error = error,
                    )
                    PinBoard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 32.dp),
                        state = state
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PinScaffold_WithDescription() {
    MauthTheme {
        PinScaffold(
            description = {
                Text("Enter PIN")
            },
            codeLength = 5,
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Unspecified)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PinScaffold_WithoutDescription() {
    MauthTheme {
        PinScaffold(
            codeLength = 5,
            windowSizeClass = WindowSizeClass.calculateFromSize(DpSize.Unspecified)
        )
    }
}