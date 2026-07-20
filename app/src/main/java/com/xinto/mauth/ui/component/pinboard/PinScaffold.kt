package com.xinto.mauth.ui.component.pinboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
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
    useMeshGradientBackground: Boolean? = false,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
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
        containerColor = if (useMeshGradientBackground == true) Color.Transparent else containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) {
        if (useMeshGradientBackground == true) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .paint(getGradientPainter())
                    .blur(64.dp)
                    .fillMaxSize()
            )
        }

        if (!windowAdaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)) {
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
                        .widthIn(
                            max = if (windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
                                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                                )
                            ) 400.dp else Dp.Unspecified
                        ),
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

@Composable
fun getGradientPainter(): MeshGradientPainter {
    // https://developer.android.com/develop/ui/compose/graphics/draw/mesh-gradient#animate-mesh-gradient
    val infiniteTransition = rememberInfiniteTransition(label = "meshMovement")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val surface = MaterialTheme.colorScheme.surface
    val surfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceContainerHghest = MaterialTheme.colorScheme.surfaceContainerHighest
    val surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow

    val gradientPainter = remember {
        MeshGradientPainter(rows = 3, columns = 3) {
            setVertex(0, 0, Offset(0.0f, 0.0f), surfaceContainerLow)
            setVertex(0, 1, Offset(0.3f, 0.0f), surfaceContainer)
            setVertex(0, 2, Offset(0.7f, 0.0f), surfaceContainerHigh)
            setVertex(0, 3, Offset(1.0f, 0.0f), surfaceContainerHghest)

            setVertex(1, 0, Offset(0.0f, 0.3f), surface)
            setVertex(1, 1, Offset(0.2f, 0.4f) + Offset(animatedOffset, animatedOffset), surface)
            setVertex(
                1,
                2,
                Offset(0.7f, 0.2f) + Offset(animatedOffset, animatedOffset),
                surfaceContainer
            )
            setVertex(1, 3, Offset(1.0f, 0.3f), surfaceContainerLow)

            setVertex(2, 0, Offset(0.0f, 0.7f), surface)
            setVertex(2, 1, Offset(0.3f, 0.8f) + Offset(animatedOffset, 0f), surface)
            setVertex(2, 2, Offset(0.7f, 0.6f) + Offset(animatedOffset, 0f), surfaceContainerHghest)
            setVertex(2, 3, Offset(1.0f, 0.7f), surfaceContainerHigh)

            setVertex(3, 0, Offset(0.0f, 1.0f), surfaceContainerHghest)
            setVertex(3, 1, Offset(0.3f, 1.0f), surfaceContainerHigh)
            setVertex(3, 2, Offset(0.7f, 1.0f), surface)
            setVertex(3, 3, Offset(1.0f, 1.0f), surfaceContainerLow)
        }
    }

    return gradientPainter
}

@Composable
@PreviewAllConfigurations
fun PinScaffold_WithDescription() {
    MauthTheme {
        PinScaffold(
            description = { Text("Enter PIN") },
            codeLength = 5
        )
    }
}

@Composable
@PreviewAllConfigurations
fun PinScaffold_WithoutDescription() {
    MauthTheme {
        PinScaffold(codeLength = 5)
    }
}
