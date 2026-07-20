package com.xinto.mauth.ui.screen.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.pinboard.PinScaffold
import com.xinto.mauth.ui.component.pinboard.rememberPinBoardState
import com.xinto.mauth.ui.component.rememberBiometricHandler
import com.xinto.mauth.ui.component.rememberBiometricPromptData
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onAuthSuccess: () -> Unit,
    onBackPress: (() -> Unit)? = null
) {
    val viewModel: AuthViewModel = koinViewModel()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val useBiometrics by viewModel.useBiometrics.collectAsStateWithLifecycle()
    val useMeshGradientBackground by viewModel.useMeshGradientBackground.collectAsStateWithLifecycle()

    val biometricHandler = rememberBiometricHandler(
        onAuthSuccess = onAuthSuccess,
    )
    val promptData = rememberBiometricPromptData(
        title = stringResource(R.string.auth_biometrics_title),
        negativeButtonText = stringResource(R.string.auth_biometrics_cancel)
    )
    val canUseBiometrics by remember(biometricHandler) {
        derivedStateOf {
            useBiometrics && biometricHandler.canUseBiometrics()
        }
    }

    BackHandler(enabled = onBackPress != null) {
        onBackPress?.invoke()
    }
    LaunchedEffect(code) {
        if (viewModel.validate(code)) {
            onAuthSuccess()
        }
    }
    DisposableEffect(biometricHandler, canUseBiometrics) {
        if (canUseBiometrics) {
            biometricHandler.requestBiometrics(promptData)
        }

        onDispose {
            biometricHandler.cancelRequest()
        }
    }
    AuthScreen(
        modifier = modifier,
        code = code,
        onNumberAdd = viewModel::insertNumber,
        onNumberDelete = viewModel::deleteNumber,
        onClear = viewModel::clear,
        showFingerprint = canUseBiometrics,
        onFingerprintClick = {
            biometricHandler.requestBiometrics(promptData)
        },
        onBackPress = onBackPress,
        useMeshGradientBackground = useMeshGradientBackground
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    code: String,
    onNumberAdd: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onClear: () -> Unit,
    showFingerprint: Boolean,
    onFingerprintClick: () -> Unit,
    onBackPress: (() -> Unit)? = null,
    useMeshGradientBackground: Boolean
) {
    val pinBoardState = rememberPinBoardState(
        showFingerprint = showFingerprint,
        onFingerprintClick = onFingerprintClick,
        onNumberClick = onNumberAdd,
        onBackspaceClick = onNumberDelete,
        onBackspaceLongClick = onClear
    )

    PinScaffold(
        modifier = modifier,
        topBar = {
            if (onBackPress != null) {
                LargeTopAppBar(
                    title = {
                        Text(stringResource(R.string.auth_title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = null
                            )
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(top = 32.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                stringResource(R.string.auth_title),
                                fontSize = 21.sp
                            )
                        }
                    }
                )
            }
        },
        codeLength = code.length,
        state = pinBoardState,
        useMeshGradientBackground = useMeshGradientBackground
    )
}

@Composable
@PreviewAllConfigurations
private fun AuthScreen_Empty_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                code = "",
                onNumberAdd = {},
                onNumberDelete = {},
                onClear = {},
                showFingerprint = false,
                onFingerprintClick = {},
                onBackPress = null,
                useMeshGradientBackground = false
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AuthScreen_Fingerprint_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                code = "",
                onNumberAdd = {},
                onNumberDelete = {},
                onClear = {},
                showFingerprint = true,
                onFingerprintClick = {},
                onBackPress = null,
                useMeshGradientBackground = false
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AuthScreen_PartialCode_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                code = "123",
                onNumberAdd = {},
                onNumberDelete = {},
                onClear = {},
                showFingerprint = true,
                onFingerprintClick = {},
                onBackPress = null,
                useMeshGradientBackground = false
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AuthScreen_WithBackButton_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AuthScreen(
                modifier = Modifier.fillMaxSize(),
                code = "12",
                onNumberAdd = {},
                onNumberDelete = {},
                onClear = {},
                showFingerprint = false,
                onFingerprintClick = {},
                onBackPress = {},
                useMeshGradientBackground = false
            )
        }
    }
}