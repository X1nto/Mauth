@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.component.pinboard

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.lerp
import androidx.graphics.shapes.Morph
import com.xinto.mauth.ui.theme.MauthTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val PinSlotPolygons = listOf(
    MaterialShapes.Cookie4Sided,
    MaterialShapes.Pentagon,
    MaterialShapes.Sunny,
    MaterialShapes.Cookie6Sided,
    MaterialShapes.Slanted,
    MaterialShapes.Gem,
    MaterialShapes.VerySunny,
    MaterialShapes.Cookie9Sided,
    MaterialShapes.Clover4Leaf,
    MaterialShapes.SoftBurst,
)

@Composable
fun PinDisplay(
    length: Int,
    modifier: Modifier = Modifier,
    error: Boolean = false,
) {
    val color by animateColorAsState(
        targetValue = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
        label = "PinSlotColor"
    )
    val settleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val shake = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(error) {
        if (error) {
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
            shake.snapTo(0f)
            shake.animateTo(targetValue = 0f, animationSpec = keyframes {
                durationMillis = 420
                0f at 0
                (-1f) at 60
                0.9f at 120
                (-0.65f) at 180
                0.45f at 240
                (-0.25f) at 300
                0f at 420
            })
        } else {
            shake.snapTo(0f)
        }
    }
    Layout(
        modifier = modifier
            .height(PinDisplayDefaults.Height)
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                translationX = shake.value * PinDisplayDefaults.ShakeDistance.toPx()
            },
        content = {
            repeat(length) { i ->
                key(i) {
                    PinSlot(
                        index = i,
                        color = color,
                        settleSpec = settleSpec,
                        isLatest = i == length - 1
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val count = measurables.size
        if (count == 0) {
            return@Layout layout(constraints.minWidth, constraints.constrainHeight(0)) {}
        }

        val maxSlot = PinDisplayDefaults.SlotSize.roundToPx()
        val maxGap = PinDisplayDefaults.SlotSpacing.roundToPx()
        val desired = count * maxSlot + (count - 1) * maxGap
        val scale = if (constraints.hasBoundedWidth && desired > constraints.maxWidth) {
            constraints.maxWidth.toFloat() / desired
        } else {
            1f
        }
        val slot = (maxSlot * scale).roundToInt().coerceAtLeast(1)
        val gap = (maxGap * scale).roundToInt()

        val placeables = measurables.fastMap { it.measure(Constraints.fixed(slot, slot)) }
        val total = count * slot + (count - 1) * gap
        val width = constraints.constrainWidth(total)
        val height = constraints.constrainHeight(slot)
        layout(width, height) {
            var x = (width - total) / 2
            val y = (height - slot) / 2
            placeables.fastForEach { placeable ->
                placeable.placeRelative(x, y)
                x += slot + gap
            }
        }
    }
}

@Composable
private fun PinSlot(
    index: Int,
    color: Color,
    settleSpec: FiniteAnimationSpec<Float>,
    isLatest: Boolean
) {
    val morph = remember(index) {
        Morph(PinSlotPolygons[index % PinSlotPolygons.size], MaterialShapes.Circle)
    }
    val pop = remember { Animatable(0f) }
    val settle = remember { Animatable(0f) }
    LaunchedEffect(pop, settle) {
        launch {
            pop.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.42f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        delay(PinDisplayDefaults.ShapeHoldMillis)
        settle.animateTo(targetValue = 1f, animationSpec = settleSpec)
    }

    val isInspecting = LocalInspectionMode.current
    Spacer(
        modifier = Modifier
            .graphicsLayer {
                val popped = if (isInspecting) 1f else pop.value
                val scale = popped * lerp(
                    1f,
                    PinDisplayDefaults.DotScale,
                    inspectedSettle(settle, isInspecting, isLatest)
                )
                scaleX = scale
                scaleY = scale
                alpha = popped.coerceIn(0f, 1f)
            }
            .drawWithCache {
                val path = Path()
                val scaleMatrix = Matrix().apply { scale(x = size.width, y = size.height) }
                onDrawBehind {
                    path.rewind()
                    morph.toPath(
                        progress = inspectedSettle(settle, isInspecting, isLatest),
                        path = path
                    )
                    path.transform(scaleMatrix)
                    path.translate(size.center - path.getBounds().center)
                    drawPath(path = path, color = color)
                }
            }
    )
}

// FIXME: This is a hack for previews to work.
private fun inspectedSettle(
    settle: Animatable<Float, *>,
    isInspecting: Boolean,
    isLatest: Boolean
): Float = when {
    !isInspecting -> settle.value.coerceIn(0f, 1f)
    isLatest -> 0f
    else -> 1f
}

private object PinDisplayDefaults {
    val Height = 64.dp
    val SlotSize = 24.dp
    val SlotSpacing = 10.dp
    val ShakeDistance = 8.dp

    const val ShapeHoldMillis = 120L

    const val DotScale = 0.8f
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinDisplay_Plain() {
    MauthTheme {
        PinDisplay(
            modifier = Modifier.width(200.dp),
            length = 4
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinDisplay_Overflowing() {
    MauthTheme {
        PinDisplay(
            modifier = Modifier.width(200.dp),
            length = 20
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinDisplay_Error() {
    MauthTheme {
        PinDisplay(
            modifier = Modifier.width(200.dp),
            length = 4,
            error = true
        )
    }
}
