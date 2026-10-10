package com.xinto.mauth.ui.component

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat

@Composable
fun adaptivePainterResource(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val adaptivePainter = remember(id, context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return@remember null
        }

        val drawable = ContextCompat.getDrawable(context, id) as? AdaptiveIconDrawable
            ?: return@remember null
        AdaptiveIconPainter(drawable)
    }
    return adaptivePainter ?: painterResource(id)
}

@RequiresApi(Build.VERSION_CODES.O)
private class AdaptiveIconPainter(
    private val drawable: AdaptiveIconDrawable
) : Painter() {

    override val intrinsicSize: Size =
        if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
            Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        } else {
            Size.Unspecified
        }

    override fun DrawScope.onDraw() {
        val bleed = 1f + 2f * AdaptiveIconDrawable.getExtraInsetFraction()
        drawLayer(drawable.background, bleed)
        drawLayer(drawable.foreground, bleed)
    }

    private fun DrawScope.drawLayer(layer: Drawable?, scale: Float) {
        if (layer == null)
            return

        val insetX = ((size.width * scale) - size.width) / 2f
        val insetY = ((size.height * scale) - size.height) / 2f
        drawIntoCanvas {
            layer.setBounds(
                (-insetX).toInt(),
                (-insetY).toInt(),
                (size.width + insetX).toInt(),
                (size.height + insetY).toInt()
            )
            layer.draw(it.nativeCanvas)
        }
    }
}
