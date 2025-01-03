package com.xinto.mauth.ui.component.pinboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xinto.mauth.ui.theme.MauthTheme

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
    description: (@Composable () -> Unit)? = null,
    error: Boolean = false,
    useSmallButtons: Boolean = false,
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
        val orientation = LocalConfiguration.current.orientation
        val minButtonSize = remember(useSmallButtons) {
            if (useSmallButtons) {
                PinButtonDefaults.PinButtonSmallMinSize
            } else {
                PinButtonDefaults.PinButtonNormalMinSize
            }
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(it)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
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
                }
                val pinBoardHorizontalPadding = 16.dp
                val horizontalButtonSpace = 16.dp
                val totalPadding = (pinBoardHorizontalPadding * 2) + (horizontalButtonSpace * 2)
                val maxBoxWidth = remember(minButtonSize) {
                    val buttonsInRow = 3
                    (minButtonSize * buttonsInRow) + totalPadding
                }
                Box(
                    modifier = Modifier.sizeIn(maxWidth = maxBoxWidth),
                    contentAlignment = Alignment.Center
                ) {
                    PinBoard(
                        modifier = Modifier.padding(horizontal = pinBoardHorizontalPadding),
                        horizontalButtonSpace = horizontalButtonSpace,
                        minButtonSize = minButtonSize,
                        state = state
                    )
                }
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(it)
                    .padding(40.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (description != null) {
                    Spacer(modifier = Modifier.weight(1f))
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
                    Spacer(modifier = Modifier.weight(1f))
                }
                PinDisplay(
                    modifier = Modifier
                        .fillMaxWidth(),
                    length = codeLength,
                    error = error,
                )
                PinBoard(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 32.dp),
                    state = state
                )
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PinScaffold_WithDescription() {
    MauthTheme {
        PinScaffold(
            description = {
                Text("Enter PIN")
            },
            codeLength = 5
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun PinScaffold_WithoutDescription() {
    MauthTheme {
        PinScaffold(
            codeLength = 5
        )
    }
}