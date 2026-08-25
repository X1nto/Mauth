package com.xinto.mauth.ui.component.pinboard

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.MeshGradientScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import com.xinto.mauth.ui.theme.isDark
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

private const val TwoPi = (2 * PI).toFloat()

private const val MeshCycleMillis = 24_000
private const val MeshRenderScale = 6f
private const val MeshFrameMillis = 50

private const val InteriorDrift = 0.10f
private const val EdgeDrift = 0.07f

private const val LightShade = 0.16f
private const val DarkLit = 0.18f

private const val DarkShadow = 0.10f
private const val DarkShadowSoft = 0.05f

private val MeshGrid = floatArrayOf(0f, 0.33f, 0.67f, 1f)
private val MeshSeedsX = floatArrayOf(
    0f, 0.4f, 1.9f, 0f,
    0f, 0.0f, 2.1f, 0f,
    0f, 3.8f, 5.2f, 0f,
    0f, 4.7f, 1.4f, 0f,
)
private val MeshSeedsY = floatArrayOf(
    0f, 0f, 0f, 0f,
    2.7f, 0.6f, 3.4f, 1.2f,
    4.4f, 5.0f, 2.3f, 0.9f,
    0f, 0f, 0f, 0f,
)

private data class MeshPalette(
    val base: Color,
    val toneA: Color,
    val toneB: Color,
    val toneC: Color,
    val shade: Color,
    val highlight: Color,
)

private fun ColorScheme.toMeshPalette(): MeshPalette {
    return if (isDark) {
        MeshPalette(
            base = surface,
            toneA = lerp(surface, surfaceTint, DarkLit * 0.70f),
            toneB = lerp(surface, surfaceTint, DarkLit * 0.35f),
            toneC = lerp(surface, Color.Black, DarkShadowSoft),
            shade = lerp(surface, Color.Black, DarkShadow),
            highlight = lerp(surface, surfaceTint, DarkLit),
        )
    } else {
        MeshPalette(
            base = lerp(surface, surfaceTint, LightShade * 0.30f),
            toneA = lerp(surface, surfaceTint, LightShade * 0.85f),
            toneB = lerp(surface, surfaceTint, LightShade * 0.45f),
            toneC = lerp(surface, surfaceTint, LightShade * 0.65f),
            shade = lerp(surface, surfaceTint, LightShade),
            highlight = surface,
        )
    }
}


@Composable
fun MeshGradientBackground(modifier: Modifier = Modifier) {
    val painter = rememberMeshGradientPainter()
    Spacer(
        modifier = modifier.drawBehind {
            scale(MeshRenderScale, pivot = Offset.Zero) {
                with(painter) {
                    draw(Size(size.width / MeshRenderScale, size.height / MeshRenderScale))
                }
            }
        }
    )
}

@Composable
private fun rememberMeshGradientPainter(): MeshGradientPainter {
    val colorScheme = MaterialTheme.colorScheme
    val palette = remember(colorScheme.surface, colorScheme.surfaceTint) {
        colorScheme.toMeshPalette()
    }

    val canAnimate = rememberAnimatorsEnabled()
    if (!canAnimate) {
        return remember(palette) {
            MeshGradientPainter(rows = 3, columns = 3, hasBicubicColor = true) {
                setMeshVertices(palette, 0f)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "meshMovement")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = TwoPi,
        animationSpec = infiniteRepeatable(
            animation = tween(MeshCycleMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val steppedPhase by remember {
        val steps = MeshCycleMillis / MeshFrameMillis
        derivedStateOf {
            floor(phase / TwoPi * steps) / steps * TwoPi
        }
    }

    return remember(palette) {
        MeshGradientPainter(rows = 3, columns = 3, hasBicubicColor = true) {
            setMeshVertices(palette, steppedPhase)
        }
    }
}

@Composable
private fun rememberAnimatorsEnabled(): Boolean {
    val context = LocalContext.current
    // Using this instead of ValueAnimator.areAnimatorsEnabled(), which is API 26+.
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
}

private fun MeshGradientScope.setMeshVertices(palette: MeshPalette, phase: Float) {
    fun vertex(row: Int, column: Int, color: Color) {
        val movesX = column in 1..2
        val movesY = row in 1..2
        val amplitude = if (movesX && movesY) InteriorDrift else EdgeDrift
        val seed = row * 4 + column
        setVertex(
            row = row,
            column = column,
            position = Offset(
                x = MeshGrid[column] + if (movesX) sin(phase + MeshSeedsX[seed]) * amplitude else 0f,
                y = MeshGrid[row] + if (movesY) sin(2 * phase + MeshSeedsY[seed]) * amplitude else 0f,
            ),
            color = color,
        )
    }

    vertex(0, 0, palette.toneA)
    vertex(0, 1, palette.base)
    vertex(0, 2, palette.toneB)
    vertex(0, 3, palette.highlight)

    vertex(1, 0, palette.shade)
    vertex(1, 1, palette.toneB)
    vertex(1, 2, palette.base)
    vertex(1, 3, palette.toneC)

    vertex(2, 0, palette.toneC)
    vertex(2, 1, palette.base)
    vertex(2, 2, palette.toneA)
    vertex(2, 3, palette.shade)

    vertex(3, 0, palette.highlight)
    vertex(3, 1, palette.toneC)
    vertex(3, 2, palette.base)
    vertex(3, 3, palette.toneB)
}