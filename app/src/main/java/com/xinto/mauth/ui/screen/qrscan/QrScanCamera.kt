package com.xinto.mauth.ui.screen.qrscan

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.xinto.mauth.R

@Immutable
data class CameraState(
    val cameraProvider: ProcessCameraProvider,
    val analysis: ImageAnalysis,
    val preview: Preview,
    val cameraSelector: CameraSelector?
)

@Composable
fun rememberCameraState(
    context: Context,
    analysis: ImageAnalysis = ImageAnalysis.Builder().build(),
    preview: Preview = Preview.Builder().build(),
): CameraState {
    return remember(context, analysis, preview) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val cameraSelector = listOf(CameraSelector.DEFAULT_BACK_CAMERA, CameraSelector.DEFAULT_FRONT_CAMERA).firstOrNull { selector ->
            try {
                cameraProvider.hasCamera(selector)
            } catch (_: Exception) {
                false
            }
        }
        CameraState(
            cameraProvider = cameraProvider,
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
    var isCameraUnavailable by remember(state) { mutableStateOf(state.cameraSelector == null) }
    DisposableEffect(state, lifecycleOwner) {
        if (state.cameraSelector != null) {
            try {
                state.cameraProvider.unbindAll()
                state.cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    state.cameraSelector,
                    state.preview, state.analysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
                isCameraUnavailable = true
            }
        }

        onDispose {
            state.cameraProvider.unbindAll()
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isCameraUnavailable) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.qrscan_camera_unavailable),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
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
                    state.preview.surfaceProvider = previewView.surfaceProvider
                }
            )
        }
    }
}