package com.xinto.mauth.ui.screen.qrscan

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.xinto.mauth.R
import com.xinto.mauth.core.camera.QrCodeAnalyzer
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.screen.qrscan.component.QrScanCamera
import com.xinto.mauth.ui.screen.qrscan.component.QrScanPermissionDeniedDialog
import com.xinto.mauth.ui.screen.qrscan.component.rememberCameraState
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executors

@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScan: (DomainAccountInfo) -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val viewModel: QrScanViewModel = koinViewModel()
    val batchData by viewModel.batchData.collectAsStateWithLifecycle()
    val scanError by viewModel.scanError.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.parseEvent.collect {
            if (it != null) {
                onScan(it)
            } else {
                onBack()
            }
        }
    }
    QrScanScreen(
        onBack = onBack,
        onScan = viewModel::parseResult,
        permissionStatus = cameraPermission.status,
        onRequestPermission = {
            cameraPermission.launchPermissionRequest()
        },
        batchData = batchData,
        scanError = scanError
    )
}

@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScan: (com.google.zxing.Result) -> Unit,
    permissionStatus: PermissionStatus,
    onRequestPermission: () -> Unit,
    batchData: BatchData,
    scanError: ScanError?
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
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        snackbarHost = {
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current
            LaunchedEffect(scanError, context) {
                if (scanError != null) {
                    snackbarHostState.showSnackbar(context.getString(scanError.stringRes))
                }
            }
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        when (permissionStatus) {
            is PermissionStatus.Granted -> {
                val context = LocalContext.current
                val cameraAnalysis = remember(context) {
                    ImageAnalysis.Builder()
                        .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                QrCodeAnalyzer(
                                    onSuccess = onScan,
                                    onFail = {}
                                )
                            )
                        }
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues)
                            .padding(horizontal = 32.dp)
                            .aspectRatio(1f / 1f),
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = 1.dp
                    ) {
                        QrScanCamera(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(MaterialTheme.shapes.large),
                            state = rememberCameraState(context, analysis = cameraAnalysis)
                        )
                    }

                    Text(
                        modifier = Modifier.alpha(if (batchData.outOf > 1) 1f else 0f),
                        text = stringResource(R.string.qrscan_info_batch, batchData.current, batchData.outOf)
                    )
                }
            }
            is PermissionStatus.Denied -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        contentDescription = null
                    )
                    Text(stringResource(R.string.qrscan_error))
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