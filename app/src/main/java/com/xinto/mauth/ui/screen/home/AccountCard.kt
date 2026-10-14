@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.ui.component.UriImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountAvatar(
    account: DomainAccount,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    val monogramStyle = MaterialTheme.typography.titleLarge
    Surface(
        modifier = modifier,
        shape = if (account.icon != null) MaterialTheme.shapes.medium else MaterialShapes.Cookie4Sided.toShape(),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (account.icon != null) {
                UriImage(uri = account.icon!!)
            } else {
                Text(
                    text = account.shortLabel,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = monogramStyle,
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 8.sp,
                        maxFontSize = monogramStyle.fontSize,
                    )
                )
            }
        }
    }
}

@Composable
fun OtpCode(
    code: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    dotSize: TextUnit = style.fontSize * 0.36f,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = code,
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
        },
        contentAlignment = Alignment.CenterEnd,
        label = "OtpCode",
    ) { animatedCode ->
        val drain = progress?.let { animateDrainAsState(it) }
        val drainModifier = if (drain == null) Modifier else Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val drained = size.width * (1f - drain.value.coerceIn(0f, 1f))
                if (drained > 0f) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        topLeft = Offset(size.width - drained, 0f),
                        size = Size(drained, size.height),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        OtpCodeGlyphs(
            modifier = drainModifier,
            code = animatedCode,
            visible = visible,
            color = color,
            style = style,
            dotSize = dotSize,
        )
    }
}

@Composable
private fun animateDrainAsState(target: Float): State<Float> {
    val progress = remember { Animatable(target) }
    LaunchedEffect(target) {
        if (target > progress.value) {
            progress.snapTo(target)
        } else {
            progress.animateTo(target, tween(durationMillis = 1000, easing = LinearEasing))
        }
    }
    return progress.asState()
}

@Composable
private fun OtpCodeGlyphs(
    code: String,
    visible: Boolean,
    color: Color,
    style: TextStyle,
    dotSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer(cacheSize = 0)
    val textLayout = remember(code, style, textMeasurer) {
        textMeasurer.measure(text = code, style = style)
    }
    val bounds = remember(textLayout) {
        FloatArray(code.length + 1) {
            if (it < code.length) {
                textLayout.getHorizontalPosition(offset = it, usePrimaryDirection = true)
            } else {
                textLayout.multiParagraph.intrinsics.maxIntrinsicWidth
            }
        }
    }
    val dotRadius = with(LocalDensity.current) { dotSize.toPx() } / 2f
    val layer = rememberGraphicsLayer()
    val reveals = remember(code.length) {
        List(code.length) {
            Animatable(if (visible) 1f else 0f)
        }
    }
    val revealSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    LaunchedEffect(visible, reveals) {
        val target = if (visible) 1f else 0f
        reveals.forEachIndexed { index, reveal ->
            launch {
                if (visible) {
                    delay(index * 25L)
                }
                reveal.animateTo(targetValue = target, animationSpec = revealSpec)
            }
        }
    }

    Layout(
        modifier = modifier
            .semantics {
                if (visible) text = AnnotatedString(code)
            }
            .drawWithCache {
                layer.record {
                    val dotStep = (bounds[code.length] - dotRadius * 2f) / maxOf(code.lastIndex, 1)
                    repeat(code.length) { index ->
                        val reveal = reveals[index].value
                        val start = bounds[index]
                        val end = bounds[index + 1]
                        val center = Offset(
                            x = lerp(
                                start = dotRadius + index * dotStep,
                                stop = (start + end) / 2f,
                                fraction = reveal
                            ),
                            y = size.height / 2f,
                        )

                        val dotAlpha = (1f - reveal / 0.6f).coerceIn(0f, 1f)
                        if (dotAlpha > 0f) {
                            drawCircle(
                                color = color,
                                radius = dotRadius * lerp(
                                    start = 1f,
                                    stop = 1.8f,
                                    fraction = reveal
                                ),
                                center = center,
                                alpha = dotAlpha,
                            )
                        }

                        val glyphAlpha = ((reveal - 0.3f) / 0.7f).coerceIn(0f, 1f)
                        if (glyphAlpha > 0f) {
                            withTransform(transformBlock = {
                                scale(
                                    scale = lerp(
                                        start = 0.5f,
                                        stop = 1f,
                                        fraction = reveal
                                    ),
                                    pivot = center
                                )
                                translate(left = center.x - (start + end) / 2f)
                                clipRect(
                                    left = if (index == 0) -size.width else start,
                                    top = -size.height,
                                    right = if (index == code.lastIndex) size.width * 2f else end,
                                    bottom = size.height * 2f,
                                )
                            }) {
                                drawText(
                                    textLayoutResult = textLayout,
                                    color = color,
                                    alpha = glyphAlpha
                                )
                            }
                        }
                    }
                }
                onDrawBehind {
                    drawLayer(layer)
                }
            }
    ) { _, constraints ->
        layout(
            width = constraints.constrainWidth(textLayout.size.width),
            height = constraints.constrainHeight(textLayout.size.height),
        ) {}
    }
}