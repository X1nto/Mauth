package com.xinto.mauth.ui.screen.export.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ExportQrCode(
    data: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        color = Color.White,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            ZxingQrImage(
                modifier = Modifier.fillMaxWidth(),
                data = data,
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        }
    }
}
