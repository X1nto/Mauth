package com.xinto.mauth.ui.component.pinboard

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun PrimaryPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.primaryPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.PrimaryShapes,
    minButtonSize: Dp = PinButtonDefaults.PinButtonNormalMinSize,
    pressHaptic: HapticFeedbackType = PinButtonDefaults.PressHaptic,
    longClickHaptic: HapticFeedbackType = PinButtonDefaults.LongClickHaptic,
    content: @Composable () -> Unit
) = PinButton(
    onClick = onClick,
    onLongClick = onLongClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    shapes = shapes,
    minButtonSize = minButtonSize,
    pressHaptic = pressHaptic,
    longClickHaptic = longClickHaptic,
    content = content
)

@Composable
fun PinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: PinButtonColors = PinButtonDefaults.plainPinButtonColors(),
    shapes: PinButtonShapes = PinButtonDefaults.PlainShapes,
    minButtonSize: Dp = PinButtonDefaults.PinButtonNormalMinSize,
    pressHaptic: HapticFeedbackType = PinButtonDefaults.PressHaptic,
    longClickHaptic: HapticFeedbackType = PinButtonDefaults.LongClickHaptic,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressProgress = animatePressProgress(interactionSource, pressHaptic)
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .sizeIn(
                minWidth = minButtonSize,
                minHeight = minButtonSize
            )
            .aspectRatio(1f)
            .drawBehind {
                val progress = pressProgress.value.coerceIn(0f, 1f)
                drawPinButtonBackground(
                    shapes = shapes,
                    color = lerp(colors.backgroundColor, colors.backgroundColorPressed, progress),
                    progress = progress
                )
            }
            .combinedClickable(
                onClick = onClick,
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource,
                onLongClick = if (onLongClick == null) null else { ->
                    haptics.performHapticFeedback(longClickHaptic)
                    onLongClick()
                },
                hapticFeedbackEnabled = false
            ),
        contentAlignment = Alignment.Center
    ) {
        PinButtonContent(
            colors = colors,
            pressProgress = pressProgress,
            content = content
        )
    }
}

@Composable
private fun PinButtonContent(
    colors: PinButtonColors,
    pressProgress: State<Float>,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.headlineLarge,
        LocalContentColor provides lerp(
            colors.foregroundColor,
            colors.foregroundColorPressed,
            pressProgress.value.coerceIn(0f, 1f)
        ),
        content = content
    )
}

object PinButtonDefaults {

    val PinButtonSmallMinSize = 48.dp
    val PinButtonNormalMinSize = 72.dp

    /**
     * `VIRTUAL_KEY`, documented as "the user has pressed a virtual on-screen key" and what
     * the AOSP keypad's `NumPadKey.doHapticKeyClick()` performs. Fired on press rather than
     * on click, matching the constant's own wording and the platform keypress guidance,
     * which pairs `VIRTUAL_KEY` on ACTION_DOWN with `VIRTUAL_KEY_RELEASE` on ACTION_UP.
     */
    val PressHaptic = HapticFeedbackType.VirtualKey

    /** What `combinedClickable` would have played on its own. */
    val LongClickHaptic = HapticFeedbackType.LongPress

    val CircleShape = RoundedCornerShape(50)

    val SquircleShape = RoundedCornerShape(30)

    val PlainShapes = PinButtonShapes(
        shape = CircleShape,
        shapePressed = SquircleShape
    )

    val PrimaryShapes = PinButtonShapes(
        shape = SquircleShape,
        shapePressed = CircleShape
    )

    @Composable
    fun plainPinButtonColors(
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        backgroundColorPressed: Color = MaterialTheme.colorScheme.primary,
        foregroundColor: Color = MaterialTheme.colorScheme.onSurface,
        foregroundColorPressed: Color = MaterialTheme.colorScheme.onPrimary
    ): PinButtonColors {
        return PinButtonColors(
            backgroundColor = backgroundColor,
            backgroundColorPressed = backgroundColorPressed,
            foregroundColor = foregroundColor,
            foregroundColorPressed = foregroundColorPressed
        )
    }

    @Composable
    fun primaryPinButtonColors(
        backgroundColor: Color = MaterialTheme.colorScheme.secondary,
        backgroundColorPressed: Color = MaterialTheme.colorScheme.primary,
        foregroundColor: Color = MaterialTheme.colorScheme.onSecondary,
        foregroundColorPressed: Color = MaterialTheme.colorScheme.onPrimary
    ): PinButtonColors {
        return PinButtonColors(
            backgroundColor = backgroundColor,
            backgroundColorPressed = backgroundColorPressed,
            foregroundColor = foregroundColor,
            foregroundColorPressed = foregroundColorPressed
        )
    }

}

@Immutable
data class PinButtonColors(
    val backgroundColor: Color,
    val backgroundColorPressed: Color,
    val foregroundColor: Color,
    val foregroundColorPressed: Color
)

@Immutable
data class PinButtonShapes(
    val shape: CornerBasedShape,
    val shapePressed: CornerBasedShape
)

private fun DrawScope.drawPinButtonBackground(
    shapes: PinButtonShapes,
    color: Color,
    progress: Float
) {
    val rest = shapes.shape
    val pressed = shapes.shapePressed
    val topStart = lerp(rest.topStart.toPx(size, this), pressed.topStart.toPx(size, this), progress)
    val topEnd = lerp(rest.topEnd.toPx(size, this), pressed.topEnd.toPx(size, this), progress)
    val bottomEnd = lerp(rest.bottomEnd.toPx(size, this), pressed.bottomEnd.toPx(size, this), progress)
    val bottomStart = lerp(rest.bottomStart.toPx(size, this), pressed.bottomStart.toPx(size, this), progress)

    if (topStart == topEnd && topEnd == bottomEnd && bottomEnd == bottomStart) {
        drawRoundRect(color = color, cornerRadius = CornerRadius(topStart))
        return
    }

    val ltr = layoutDirection == LayoutDirection.Ltr
    drawOutline(
        outline = Outline.Rounded(
            RoundRect(
                rect = size.toRect(),
                topLeft = CornerRadius(if (ltr) topStart else topEnd),
                topRight = CornerRadius(if (ltr) topEnd else topStart),
                bottomRight = CornerRadius(if (ltr) bottomEnd else bottomStart),
                bottomLeft = CornerRadius(if (ltr) bottomStart else bottomEnd)
            )
        ),
        color = color
    )
}

@Composable
private fun animatePressProgress(
    interactionSource: InteractionSource,
    pressHaptic: HapticFeedbackType
): State<Float> {
    val spec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val haptics = LocalHapticFeedback.current
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(interactionSource, animatable, spec, haptics, pressHaptic) {
        interactionSource.interactions
            .filterIsInstance<PressInteraction>()
            .collectLatest { interaction ->
                if (interaction is PressInteraction.Press) {
                    // Played here rather than from onClick so the key answers the finger
                    // landing instead of lifting. This collector already owns the press
                    // stream, so it costs no extra coroutine.
                    haptics.performHapticFeedback(pressHaptic)
                    animatable.animateTo(targetValue = 1f, animationSpec = spec)
                } else {
                    if (animatable.value < 1f) {
                        animatable.animateTo(targetValue = 1f, animationSpec = spec)
                    }
                    animatable.animateTo(targetValue = 0f, animationSpec = spec)
                }
            }
    }
    return animatable.asState()
}