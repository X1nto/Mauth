package com.xinto.mauth.ui.component

import android.content.res.Configuration
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.theme.MauthTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_Plain() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(),
                onNumberClick = {},
                onBackspaceClick = {},
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithFingerprint() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(showFingerprint = true),
                onNumberClick = {},
                onBackspaceClick = {},
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithEnter() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(showEnter = true),
                onNumberClick = {},
                onBackspaceClick = {},
            )
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithFingerprintAndEnter() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinBoard(
                state = rememberPinBoardState(
                    showFingerprint = true,
                    showEnter = true,
                ),
                onNumberClick = {},
                onBackspaceClick = {},
            )
        }
    }
}

@Immutable
data class PinBoardState(
    val showFingerprint: Boolean,
    val showEnter: Boolean
) {

    val buttons = buildList {
        ('1'..'9').forEach {
            add(PinBoardButton.Number(it))
        }

        if (showFingerprint) {
            add(PinBoardButton.Fingerprint)
        } else if (showEnter) {
            add(PinBoardButton.Backspace)
        } else {
            add(PinBoardButton.Empty)
        }

        add(PinBoardButton.Number('0'))

        if (showEnter) {
            add(PinBoardButton.Enter)
        } else {
            add(PinBoardButton.Backspace)
        }
    }

    sealed interface PinBoardButton {

        @JvmInline
        value class Number(val number: Char) : PinBoardButton {
            override fun toString() = number.toString()
        }

        data object Fingerprint : PinBoardButton
        data object Backspace : PinBoardButton
        data object Enter : PinBoardButton
        data object Empty : PinBoardButton
    }

    companion object {
        fun Saver(): Saver<PinBoardState, Any> {
            return listSaver(
                save = { listOf(it.showFingerprint, it.showEnter) },
                restore = {
                    PinBoardState(it[0], it[1])
                }
            )
        }
    }
}

@Composable
fun rememberPinBoardState(
    showFingerprint: Boolean = false,
    showEnter: Boolean = false
): PinBoardState {
    return rememberSaveable(
        showFingerprint, showEnter,
        saver = PinBoardState.Saver()
    ) {
        PinBoardState(showFingerprint, showEnter)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PinBoard(
    modifier: Modifier = Modifier,
    onNumberClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit = {},
    onEnterClick: () -> Unit = {},
    onFingerprintClick: () -> Unit = {},
    state: PinBoardState = rememberPinBoardState()
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 3
    ) {
        state.buttons.forEach {
            when (it) {
                is PinBoardState.PinBoardButton.Number -> {
                    PinButton(onClick = { onNumberClick(it.number) }) {
                        Text(it.toString())
                    }
                }
                is PinBoardState.PinBoardButton.Fingerprint -> {
                    PrimaryPinButton(onClick = onFingerprintClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fingerprint),
                            contentDescription = null
                        )
                    }
                }
                is PinBoardState.PinBoardButton.Backspace -> {
                    PrimaryPinButton(onClick = onBackspaceClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_backspace),
                            contentDescription = null
                        )
                    }
                }
                is PinBoardState.PinBoardButton.Enter -> {
                    PrimaryPinButton(onClick = onEnterClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tab),
                            contentDescription = null
                        )
                    }
                }
                is PinBoardState.PinBoardButton.Empty -> {
                    Spacer(Modifier.size(PinButtonDefaults.PinButtonSize))
                }
            }
        }
    }
}

@Composable
private fun PrimaryPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.primaryPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.plainPinButtonShapes(),
    content: @Composable () -> Unit
) = PinButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    shapes = shapes,
    content = content
)
@Composable
private fun PinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.plainPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.plainPinButtonShapes(),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape by shapes.getButtonShape(interactionSource)
    val backgroundColor by colors.getBackgroundColor(interactionSource)
    val contentColor by colors.getForegroundColor(interactionSource)
    Box(
        modifier = modifier
            .requiredSize(PinButtonDefaults.PinButtonSize)
            .graphicsLayer {
                clip = true
                this.shape = shape
            }
            .drawBehind {
                drawRect(backgroundColor)
            }
            .clickable(
                onClick = onClick,
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineSmall,
            LocalContentColor provides contentColor,
            content = content
        )
    }
}

object PinButtonDefaults {

    val PinButtonSize = 64.dp
    const val AnimationDurationPress = 200
    const val AnimationDurationRelease = 150

    @Composable
    fun plainPinButtonColors(): PinButtonColors {
        return PinButtonColors(
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            backgroundColorPressed = MaterialTheme.colorScheme.primary,
            foregroundColor = MaterialTheme.colorScheme.onSurface,
            foregroundColorPressed = MaterialTheme.colorScheme.onPrimary
        )
    }

    @Composable
    fun primaryPinButtonColors(): PinButtonColors {
        return PinButtonColors(
            backgroundColor = MaterialTheme.colorScheme.secondary,
            backgroundColorPressed = MaterialTheme.colorScheme.primary,
            foregroundColor = MaterialTheme.colorScheme.onSecondary,
            foregroundColorPressed = MaterialTheme.colorScheme.onPrimary
        )
    }

    @Composable
    fun plainPinButtonShapes(): PinButtonShapes {
        return PinButtonShapes(
            shape = RoundedCornerShape(50),
            shapePressed = MaterialTheme.shapes.large
        )
    }

}

@Stable
data class PinButtonColors(
    val backgroundColor: Color,
    val backgroundColorPressed: Color,
    val foregroundColor: Color,
    val foregroundColorPressed: Color
) {
    @Composable
    fun getBackgroundColor(interactionSource: InteractionSource): State<Color> {
        val animatable = remember { Animatable(backgroundColor) }
        return animatePressValue(
            animatable = animatable,
            initialValue = backgroundColor,
            targetValue = backgroundColorPressed,
            interactionSource = interactionSource
        )
    }

    @Composable
    fun getForegroundColor(interactionSource: InteractionSource): State<Color> {
        val animatable = remember { Animatable(foregroundColor) }
        return animatePressValue(
            animatable = animatable,
            initialValue = foregroundColor,
            targetValue = foregroundColorPressed,
            interactionSource = interactionSource
        )
    }
}

@Stable
data class PinButtonShapes(
    val shape: CornerBasedShape,
    val shapePressed: CornerBasedShape
) {

    @Composable
    fun getButtonShape(interactionSource: InteractionSource): State<CornerBasedShape> {
        val density = LocalDensity.current
        val size = with(density) {
            val shapeSize = PinButtonDefaults.PinButtonSize.toPx()
            Size(shapeSize, shapeSize)
        }

        val animatable = remember(density, size) { Animatable(shape, density, size) }
        return animatePressValue(
            animatable = animatable,
            initialValue = shape,
            targetValue = shapePressed,
            interactionSource = interactionSource
        )
    }

}

@Composable
inline fun <reified T, V : AnimationVector> animatePressValue(
    animatable: Animatable<T, V>,
    initialValue: T,
    targetValue: T,
    interactionSource: InteractionSource
): State<T> {
    LaunchedEffect(interactionSource, initialValue, targetValue, animatable) {
        val channel = Channel<Unit>(1, onBufferOverflow = BufferOverflow.DROP_LATEST)
        launch {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Press) {
                    animatable.animateTo(
                        targetValue = targetValue,
                        animationSpec = tween(PinButtonDefaults.AnimationDurationPress),
                    )
                    channel.send(Unit)
                }
            }
        }
        launch {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Release) {
                    try {
                        channel.receive()
                        animatable.animateTo(
                            targetValue = initialValue,
                            animationSpec = tween(PinButtonDefaults.AnimationDurationRelease)
                        )
                    } catch (e: CancellationException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    return animatable.asState()
}