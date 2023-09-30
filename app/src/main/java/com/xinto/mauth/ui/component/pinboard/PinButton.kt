package com.xinto.mauth.ui.component.pinboard

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.xinto.mauth.ui.component.Animatable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@Composable
fun PrimaryPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.primaryPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.plainPinButtonShapes(),
    content: @Composable () -> Unit
) = PinButton(
    onClick = onClick,
    onLongClick = onLongClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    shapes = shapes,
    content = content
)
@Composable
fun PinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
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
            .aspectRatio(1f)
            .sizeIn(
                minWidth = PinButtonDefaults.PinButtonMinSize,
                minHeight = PinButtonDefaults.PinButtonMinSize,
            )
            .graphicsLayer {
                clip = true
                this.shape = shape
            }
            .drawBehind {
                drawRect(backgroundColor)
            }
            .combinedClickable(
                onClick = onClick,
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineMedium,
            LocalContentColor provides contentColor,
            content = content
        )
    }
}

object PinButtonDefaults {

    val PinButtonMinSize = 72.dp
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
            val shapeSize = PinButtonDefaults.PinButtonMinSize.toPx()
            Size(shapeSize, shapeSize)
        }

        val animatable = remember(density, size) {
            Animatable(shape, density, size)
        }
        return animatePressValue(
            animatable = animatable,
            initialValue = shape,
            targetValue = shapePressed,
            interactionSource = interactionSource
        )
    }

}

@Composable
private fun <T, V : AnimationVector> animatePressValue(
    animatable: Animatable<T, V>,
    initialValue: T,
    targetValue: T,
    interactionSource: InteractionSource
): State<T> {
    LaunchedEffect(interactionSource, initialValue, targetValue) {
        val channel = Channel<Unit>(1, onBufferOverflow = BufferOverflow.DROP_LATEST) {
            println(it)
        }
        launch {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Press) {
                    if (animatable.value != targetValue) { //fix animation deadlock
                        animatable.animateTo(
                            targetValue = targetValue,
                            animationSpec = tween(PinButtonDefaults.AnimationDurationPress),
                        )
                    }
                    channel.send(Unit)
                }
            }
        }
        launch {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Release || it is PressInteraction.Cancel) {
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