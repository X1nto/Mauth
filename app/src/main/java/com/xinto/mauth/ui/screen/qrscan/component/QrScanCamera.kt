package com.xinto.mauth.ui.screen.qrscan.component

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@Stable
data class CameraState(
    val cameraProvider: ProcessCameraProvider,
    val analysis: ImageAnalysis,
    val preview: Preview,
    val cameraSelector: CameraSelector
)

@Composable
fun rememberCameraState(
    context: Context,
    analysis: ImageAnalysis = ImageAnalysis.Builder().build(),
    preview: Preview = Preview.Builder().build(),
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
): CameraState {
    return remember(context, analysis, preview, cameraSelector) {
        CameraState(
            cameraProvider = ProcessCameraProvider.getInstance(context).get(),
            analysis = analysis,
            preview = preview,
            cameraSelector = cameraSelector
        )
    }
}

@Composable
fun QrScanCamera(
    modifier: Modifier = Modifier,
    state: CameraState = rememberCameraState(LocalContext.current),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(state, lifecycleOwner) {
        state.cameraProvider.unbindAll()
        state.cameraProvider.bindToLifecycle(
            lifecycleOwner,
            state.cameraSelector,
            state.preview,
            state.analysis
        )

        onDispose {
            state.cameraProvider.unbindAll()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            update = { previewView ->
                state.preview.setSurfaceProvider(previewView.surfaceProvider)
            }
        )
    }
}