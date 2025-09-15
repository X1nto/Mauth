package com.xinto.mauth.ui.screen.export.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(data) {
        bitmap = null
        bitmap = ZxingEncoder.encodeToBitmap(
            data = data,
            size = size,
            backgroundColor = backgroundColor.toArgb(),
            dataColor = contentColor.toArgb()
        ).asImageBitmap()
    }
    if (bitmap != null) {
        Image(
            modifier = modifier
                .aspectRatio(1f)
                .fillMaxSize(),
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


