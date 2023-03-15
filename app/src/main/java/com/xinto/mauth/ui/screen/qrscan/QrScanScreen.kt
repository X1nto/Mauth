package com.xinto.mauth.ui.screen.qrscan

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.screen.qrscan.component.QrScanPermissionDeniedDialog
import com.xinto.mauth.ui.screen.qrscan.state.QrScanPermissionDenied
import com.xinto.mauth.ui.screen.qrscan.state.QrScanPermissionGranted
import org.koin.androidx.compose.koinViewModel

@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScan: (DomainAccountInfo) -> Unit
) {
    val cameraPermission = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )
    val viewModel: QrScanViewModel = koinViewModel()
    QrScanScreen(
        onBack = onBack,
        onScan = {
            viewModel.parseResult(it)?.let { parsedInfo ->
                onScan(parsedInfo)
            }
        },
        permissionStatus = cameraPermission.status,
        onRequestPermission = {
            cameraPermission.launchPermissionRequest()
        }
    )
}

@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScan: (com.google.zxing.Result) -> Unit,
    permissionStatus: PermissionStatus,
    onRequestPermission: () -> Unit,
) {
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialogRationale by remember { mutableStateOf(false) }
    LaunchedEffect(permissionStatus) {
        if (permissionStatus is PermissionStatus.Denied) {
            showPermissionDeniedDialog = true
            showPermissionDeniedDialogRationale = permissionStatus.shouldShowRationale
        }
    }
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.qrscan_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            when (permissionStatus) {
                is PermissionStatus.Granted -> {
                    QrScanPermissionGranted(onScan = onScan)
                }
                is PermissionStatus.Denied -> {
                    QrScanPermissionDenied()
                }
            }
        }
    }

    if (showPermissionDeniedDialog) {
        QrScanPermissionDeniedDialog(
            shouldShowRationale = showPermissionDeniedDialogRationale,
            onGrantPermission = {
                showPermissionDeniedDialog = false
                onRequestPermission()
            },
            onCancel = {
                showPermissionDeniedDialog = false
            }
        )
    }
}