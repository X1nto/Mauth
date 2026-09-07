package com.xinto.mauth.ui.screen.export.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.xinto.mauth.core.camera.ZxingEncoder

@Composable
fun ZxingQrImage(
    data: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black,
    contentScale: ContentScale = ContentScale.Fit
) {
    val painter = remember(data, backgroundColor, contentColor) {
        val bitmap = ZxingEncoder.encodeToBitmap(
            data = data,
            backgroundColor = backgroundColor.toArgb(),
            dataColor = contentColor.toArgb()
        ).asImageBitmap()
        BitmapPainter(bitmap, filterQuality = FilterQuality.None)
    }

    Image(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        painter = painter,
        contentDescription = null,
        contentScale = contentScale
    )
}
