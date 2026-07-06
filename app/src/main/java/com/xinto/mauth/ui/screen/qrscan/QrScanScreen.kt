package com.xinto.mauth.ui.screen.qrscan

import android.Manifest
import android.content.Intent
import android.net.Uri.fromParts
import android.provider.Settings
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.xinto.mauth.R
import com.xinto.mauth.core.camera.QrCodeAnalyzer
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScan: (DomainAccountInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
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
        modifier = modifier,
        onBack = onBack,
        onScan = viewModel::parseResult,
        permissionStatus = cameraPermission.status,
        permissionRequested = permissionRequested,
        onRequestPermission = {
            permissionRequested = true
            cameraPermission.launchPermissionRequest()
        },
        onOpenAppSettings = {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                fromParts("package", context.packageName, null)
            )
            context.startActivity(intent)
        },
        batchData = batchData,
        scanError = scanError
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QrScanScreen(
    onBack: () -> Unit,
    onScan: (com.google.zxing.Result) -> Unit,
    permissionStatus: PermissionStatus,
    permissionRequested: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    batchData: BatchData,
    scanError: ScanError?,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.qrscan_title)) },
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
                val permanentlyDenied = permissionRequested && !permissionStatus.shouldShowRationale
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 64.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(72.dp),
                        painter = painterResource(R.drawable.ic_error),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.qrscan_error),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(
                            when {
                                permanentlyDenied -> R.string.qrscan_permissions_subtitle_permanent
                                permissionStatus.shouldShowRationale -> R.string.qrscan_permissions_subtitle_rationale
                                else -> R.string.qrscan_permissions_subtitle
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    if (permanentlyDenied) {
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = onOpenAppSettings,
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Icon(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = null
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.qrscan_permissions_button_settings))
                        }
                    } else {
                        Button(
                            modifier = Modifier.padding(top = 8.dp),
                            onClick = onRequestPermission,
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Text(stringResource(R.string.qrscan_permissions_button_grant))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@PreviewAllConfigurations
private fun QrScanScreen_PermissionDenied_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QrScanScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                onScan = {},
                permissionStatus = PermissionStatus.Denied(shouldShowRationale = false),
                permissionRequested = false,
                onRequestPermission = {},
                onOpenAppSettings = {},
                batchData = BatchData(current = 1, outOf = 1),
                scanError = null
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@PreviewAllConfigurations
private fun QrScanScreen_PermissionDeniedRationale_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QrScanScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                onScan = {},
                permissionStatus = PermissionStatus.Denied(shouldShowRationale = true),
                permissionRequested = true,
                onRequestPermission = {},
                onOpenAppSettings = {},
                batchData = BatchData(current = 1, outOf = 1),
                scanError = null
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@PreviewAllConfigurations
private fun QrScanScreen_PermissionPermanentlyDenied_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            QrScanScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = {},
                onScan = {},
                permissionStatus = PermissionStatus.Denied(shouldShowRationale = false),
                permissionRequested = true,
                onRequestPermission = {},
                onOpenAppSettings = {},
                batchData = BatchData(current = 1, outOf = 1),
                scanError = null
            )
        }
    }
}