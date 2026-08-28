package com.xinto.mauth.ui.screen.export.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.xinto.mauth.core.camera.ZxingEncoder

@Composable
fun ZxingQrImage(
    data: String,
    modifier: Modifier = Modifier,
    size: Int = 256,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color.Black,
    contentScale: ContentScale = ContentScale.Fit
) {
    val background = backgroundColor.toArgb()
    val content = contentColor.toArgb()
    val bitmap = remember(data, size, background, content) {
        ZxingEncoder.encodeToBitmap(
            data = data,
            size = size,
            backgroundColor = background,
            dataColor = content
        ).asImageBitmap()
    }

    Image(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize(),
        bitmap = bitmap,
        contentDescription = null,
        contentScale = contentScale
    )
}
