package com.xinto.mauth.ui.component

import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity

@Composable
fun animateRoundedCornerShapeAsState(
    targetValue: CornerBasedShape
): State<CornerBasedShape> {
    val density = LocalDensity.current
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = TwoWayConverter(
            convertToVector = {
                AnimationVector(
                    v1 = it.topStart.toPx(Size.Unspecified, density),
                    v2 = it.topEnd.toPx(Size.Unspecified, density),
                    v3 = it.bottomStart.toPx(Size.Unspecified, density),
                    v4 = it.bottomEnd.toPx(Size.Unspecified, density)
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
    )
}