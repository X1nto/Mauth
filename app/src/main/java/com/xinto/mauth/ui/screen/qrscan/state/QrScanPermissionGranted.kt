package com.xinto.mauth.ui.screen.qrscan.state

import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.xinto.mauth.core.camera.QrCodeAnalyzer
import com.xinto.mauth.ui.screen.qrscan.component.QrScanCamera
import com.xinto.mauth.ui.screen.qrscan.component.rememberCameraState

@Composable
fun QrScanPermissionGranted(
    onScan: (com.google.zxing.Result) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(32.dp)
            .aspectRatio(1f / 1f),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            val context = LocalContext.current
            val cameraAnalysis = remember(context) {
                ImageAnalysis.Builder()
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            QrCodeAnalyzer(
                                onSuccess = onScan,
                                onFail = {}
                            )
                        )
                    }
            }
            QrScanCamera(
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp)
                    .clip(MaterialTheme.shapes.large),
                state = rememberCameraState(context, analysis = cameraAnalysis)
            )
        }
    }
}