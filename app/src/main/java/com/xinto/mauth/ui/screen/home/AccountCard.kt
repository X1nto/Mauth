@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.ui.component.UriImage
import kotlin.math.roundToInt

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
    progress: (() -> Float)? = null,
    color: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    dotSize: TextUnit = style.fontSize * 0.36f,
    dotGap: TextUnit = style.fontSize * 0.23f,
) {
    val target = remember(visible, code) {
        Pair(visible, code)
    }
    AnimatedContent(
        modifier = modifier,
        targetState = target,
        transitionSpec = {
            if (initialState.first == targetState.first) {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeIn() togetherWith
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
            }
        },
        contentAlignment = Alignment.CenterEnd,
        label = "OtpCode",
    ) { (show, animatedCode) ->
        val drainModifier = if (progress == null) Modifier else Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val drained = size.width * (1f - progress().coerceIn(0f, 1f))
                if (drained > 0f) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        topLeft = Offset(size.width - drained, 0f),
                        size = Size(drained, size.height),
                        blendMode = BlendMode.DstIn,
                    )
                }
            }
        if (show) {
            Text(
                modifier = drainModifier,
                text = animatedCode,
                color = color,
                style = style,
                maxLines = 1,
                softWrap = false
            )
        } else {
            OtpCodeDots(
                modifier = drainModifier,
                count = animatedCode.length,
                color = color,
                dotSize = dotSize,
                dotGap = dotGap,
                height = style.lineHeight.takeOrElse { style.fontSize * 1.5f },
            )
        }
    }
}

@Composable
private fun OtpCodeDots(
    count: Int,
    color: Color,
    dotSize: TextUnit,
    dotGap: TextUnit,
    height: TextUnit,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier.drawBehind {
            val diameter = dotSize.toPx()
            val radius = diameter / 2f
            val step = diameter + dotGap.toPx()
            repeat(count) { index ->
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(
                        x = index * step + radius,
                        y = size.height / 2f,
                    ),
                )
            }
        }
    ) { _, constraints ->
        val width = dotSize.toPx() * count + dotGap.toPx() * (count - 1)
        layout(
            width = constraints.constrainWidth(width.roundToInt()),
            height = constraints.constrainHeight(height.toPx().roundToInt()),
        ) {}
    }
}