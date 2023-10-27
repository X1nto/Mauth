package com.xinto.mauth.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

fun getShapeConverter(size: Size, density: Density): TwoWayConverter<CornerBasedShape, AnimationVector4D> {
    return TwoWayConverter(
        convertToVector = {
            AnimationVector(
                v1 = it.topStart.toPx(size, density),
                v2 = it.topEnd.toPx(size, density),
                v3 = it.bottomStart.toPx(size, density),
                v4 = it.bottomEnd.toPx(size, density)
            )
        },
        convertFromVector = {
            RoundedCornerShape(
                topStart = it.v1,
                topEnd = it.v2,
                bottomStart = it.v3,
                bottomEnd = it.v4
            )
        }
    )
}

val RoundedCornerShapeVisibilityThreshold = RoundedCornerShape(0.5f)

@Composable
fun animateRoundedCornerShapeAsState(
    targetValue: CornerBasedShape,
    size: Size = Size.Unspecified,
    visibilityThreshold: CornerBasedShape = RoundedCornerShapeVisibilityThreshold,
    animationSpec: AnimationSpec<CornerBasedShape> = remember { spring() },
    label: String = "ShapeAnimation",
    finishedListener: ((CornerBasedShape) -> Unit)? = null
): State<CornerBasedShape> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = getShapeConverter(size, LocalDensity.current),
        animationSpec = animationSpec,
        visibilityThreshold = visibilityThreshold,
        label = label,
        finishedListener = finishedListener
    )
}

@Composable
inline fun <S> Transition<S>.animateRoundedCornerShape(
    size: Size = Size.Unspecified,
    noinline transitionSpec: @Composable Transition.Segment<S>.() -> FiniteAnimationSpec<CornerBasedShape> = {
        spring(visibilityThreshold = RoundedCornerShapeVisibilityThreshold)
    },
    label: String = "OffsetAnimation",
    targetValueByState: @Composable (state: S) -> CornerBasedShape
): State<CornerBasedShape> {
    return animateValue(
        typeConverter = getShapeConverter(size, LocalDensity.current),
        transitionSpec = transitionSpec,
        label = label,
        targetValueByState = targetValueByState
    )
}

fun Animatable(
    initialValue: CornerBasedShape,
    density: Density,
    size: Size
): Animatable<CornerBasedShape, AnimationVector4D> = Animatable(
    initialValue = initialValue,
    typeConverter = getShapeConverter(size, density)
)