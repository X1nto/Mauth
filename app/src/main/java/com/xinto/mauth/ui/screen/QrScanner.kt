package com.xinto.mauth.ui.screen

import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.xinto.mauth.R
import com.xinto.mauth.camera.analyzer.QrCodeAnalyzer
import com.xinto.mauth.ui.camera.CameraPreview
import com.xinto.mauth.ui.camera.rememberCameraState
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.QrScannerViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun QrScannerScreen(
    navigator: MauthNavigator,
    viewModel: QrScannerViewModel = getViewModel()
) {
    val cameraPermission = rememberPermissionState(
        permission = android.Manifest.permission.CAMERA
    )
    val context = LocalContext.current
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
                            CameraPreview(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(12.dp)
                                    .clip(MaterialTheme.shapes.large),
                                state = rememberCameraState(
                                    context = context,
                                    analysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(
                                                ContextCompat.getMainExecutor(context),
                                                QrCodeAnalyzer(
                                                    onSuccess = {
                                                        val params = viewModel.parseOtpUri(it.text)
                                                        if (params != null) {
                                                            navigator.replace(
                                                                MauthDestination.AddAccount(
                                                                    params
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onFail = {}
                                                )
                                            )
                                        }
                                )
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