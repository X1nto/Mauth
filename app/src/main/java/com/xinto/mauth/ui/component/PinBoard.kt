package com.xinto.mauth.ui.component

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.ui.theme.MauthTheme

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithFingerprint() {
    MauthTheme {
        PinBoard(
            onEnterNumber = {},
            onDeleteNumber = {},
            showFingerprint = true
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PinBoardPreview_WithoutFingerprint() {
    MauthTheme {
        PinBoard(
            onEnterNumber = {},
            onDeleteNumber = {},
            showFingerprint = false,
        )
    }
}

private sealed interface PinBoardButton {
    
    @JvmInline
    value class Number(val number: Char) : PinBoardButton {
        override fun toString() = number.toString()
    }

    data object Fingerprint : PinBoardButton

    data object Backspace : PinBoardButton
    
    companion object {
        val buttons = buildList {
            ('1'..'9').forEach {
                add(Number(it))
            }
            add(Fingerprint)
            add(Number('0'))
            add(Backspace)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PinBoard(
    modifier: Modifier = Modifier,
    onEnterNumber: (Char) -> Unit,
    onDeleteNumber: () -> Unit,
    showFingerprint: Boolean = false,
    enableFingerprint: Boolean = true,
    onFingerprintClick: () -> Unit = {},
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 3
    ) {
        PinBoardButton.buttons.forEach {
            when (it) {
                is PinBoardButton.Number -> {
                    PinButton(onClick = { onEnterNumber(it.number) }) {
                        Text(it.toString())
                    }
                }
                is PinBoardButton.Fingerprint -> {
                    PrimaryPinButton(
                        modifier = Modifier.graphicsLayer {
                            alpha = if (showFingerprint) 1f else 0f
                        },
                        onClick = onFingerprintClick,
                        enabled = enableFingerprint && showFingerprint,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fingerprint),
                            contentDescription = null
                        )
                    }
                }
                is PinBoardButton.Backspace -> {
                    PrimaryPinButton(
                        onClick = onDeleteNumber,
                        enabled = true,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_backspace),
                            contentDescription = null
                        )
                    }
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
    val color by colors.getBackgroundColor(interactionSource)
    val contentColor by colors.getForegroundColor(interactionSource)
    Surface(
        modifier = modifier
            .requiredSize(PinButtonDefaults.PinButtonSize),
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        color = color,
        contentColor = contentColor,
        enabled = enabled,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            propagateMinConstraints = false,
            contentAlignment = Alignment.Center
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
                content()
            }
        }
    }
}

object PinButtonDefaults {

    val PinButtonSize = 64.dp

    @Composable
    fun plainPinButtonColors(): PinButtonColors {
        return PinButtonColors(
            backgroundColor = MaterialTheme.colorScheme.surface,
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
        val pressed by interactionSource.collectIsPressedAsState()
        return animateColorAsState(
            targetValue = if (pressed) backgroundColorPressed else backgroundColor,
            animationSpec = tween(200),
            label = "PinButtonBackground"
        )
    }

    @Composable
    fun getForegroundColor(interactionSource: InteractionSource): State<Color> {
        val pressed by interactionSource.collectIsPressedAsState()
        return animateColorAsState(
            targetValue = if (pressed) foregroundColorPressed else foregroundColor,
            animationSpec = tween(200),
            label = "PinButtonForeground"
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
        val isPressed by interactionSource.collectIsPressedAsState()
        val density = LocalDensity.current
        val size = with(density) {
            val shapeSize = PinButtonDefaults.PinButtonSize.toPx()
            Size(shapeSize, shapeSize)
        }
        return animateRoundedCornerShapeAsState(
            targetValue = if (isPressed) shapePressed else shape,
            size = size,
            animationSpec = tween(200)
        )
    }

}