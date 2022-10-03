package com.xinto.mauth.ui.screen

import android.content.Context
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.xinto.mauth.R
import com.xinto.mauth.camera.analyzer.QrCodeAnalyzer
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.QrScannerViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun QrScannerScreen(
    navigator: MauthNavigator,
    viewModel: QrScannerViewModel = getViewModel()
) {
    val cameraPermission = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    BackHandler {
        navigator.pop()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.qrscan_title))
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (val status = cameraPermission.status) {
                is PermissionStatus.Granted -> {
                    Surface(
                        modifier = Modifier
                            .padding(32.dp)
                            .aspectRatio(1f / 1f),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Camera(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(12.dp)
                                    .clip(MaterialTheme.shapes.large),
                                onQrResult = { cameraProvider, qr ->
                                    val params = viewModel.parseOtpUri(qr.text)
                                    if (params != null) {
                                        cameraProvider.unbindAll()
                                        navigator.replace(MauthDestination.AddAccount(params))
                                    }
                                }
                            )
                        }
                    }
                }
                is PermissionStatus.Denied -> {
                    AlertDialog(
                        onDismissRequest = { /*TODO*/ },
                        text = {
                            if (status.shouldShowRationale) {
                                Text(stringResource(R.string.qrscan_permissions_subtitle_rationale))
                            } else {
                                Text(stringResource(R.string.qrscan_permissions_subtitle))
                            }
                        },
                        confirmButton = {
                            Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                                Text(stringResource(R.string.qrscan_permissions_button_grant))
                            }
                        },
                        dismissButton = {
                            Button(onClick = { navigator.pop() }) {
                                Text(stringResource(R.string.qrscan_permissions_button_cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Camera(
    modifier: Modifier = Modifier,
    onQrResult: (ProcessCameraProvider, com.google.zxing.Result) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

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
                coroutineScope.launch {
                    val cameraProvider = getCameraProvider(context)

                    val preview = Preview.Builder()
                        .build()
                        .also { preview ->
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                QrCodeAnalyzer(
                                    onSuccess = {
                                        onQrResult(cameraProvider, it)
                                    },
                                    onFail = {}
                                )
                            )
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                }
            }
        )
    }
}

private suspend fun getCameraProvider(context: Context): ProcessCameraProvider {
    return suspendCoroutine { continuation ->
        continuation.resume(
            ProcessCameraProvider.getInstance(context).get()
        )
    }
}