package com.xinto.mauth.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun UriImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    Image(
        modifier = modifier,
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(uri)
                .build()
        ),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop
    )
}