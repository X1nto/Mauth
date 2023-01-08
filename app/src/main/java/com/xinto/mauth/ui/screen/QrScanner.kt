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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.zxing.Result
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
    BackHandler {
        navigator.pop()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Topbar(
                onBackClick = {
                    navigator.pop()
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
                    PermissionGranted(onSuccessScan = {
                        viewModel.acceptSuccessScan(it)?.let { accountInfo ->
                            navigator.replace(MauthDestination.AddAccount(accountInfo))
                        }
                    })
                }
                is PermissionStatus.Denied -> {
                    PermissionDeniedDialog(
                        shouldShowRationale = status.shouldShowRationale,
                        onPermissionGrantClick = {
                            cameraPermission.launchPermissionRequest()
                        },
                        onCancelClick = {
                            navigator.pop()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Topbar(
    onBackClick: () -> Unit,
) {
    LargeTopAppBar(
        title = {
            Text(stringResource(R.string.qrscan_title))
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun PermissionGranted(
    onSuccessScan: (Result) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(32.dp)
            .aspectRatio(1f / 1f),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Scanner(
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp)
                    .clip(MaterialTheme.shapes.large),
                onSuccessScan = onSuccessScan
            )
        }
    }
}

@Composable
private fun PermissionDeniedDialog(
    shouldShowRationale: Boolean,
    onPermissionGrantClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        text = {
            if (shouldShowRationale) {
                Text(stringResource(R.string.qrscan_permissions_subtitle_rationale))
            } else {
                Text(stringResource(R.string.qrscan_permissions_subtitle))
            }
        },
        confirmButton = {
            Button(onClick = onPermissionGrantClick) {
                Text(stringResource(R.string.qrscan_permissions_button_grant))
            }
        },
        dismissButton = {
            Button(onClick = onCancelClick) {
                Text(stringResource(R.string.qrscan_permissions_button_cancel))
            }
        }
    )
}

@Composable
private fun Scanner(
    modifier: Modifier = Modifier,
    onSuccessScan: (Result) -> Unit,
) {
    val context = LocalContext.current
    val cameraAnalysis = remember(context) {
        ImageAnalysis.Builder()
            .build()
            .also { analysis ->
                analysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrCodeAnalyzer(
                        onSuccess = onSuccessScan,
                        onFail = {}
                    )
                )
            }
    }
    val cameraState = rememberCameraState(context = context, analysis = cameraAnalysis)
    CameraPreview(
        modifier = modifier,
        state = cameraState
    )
}