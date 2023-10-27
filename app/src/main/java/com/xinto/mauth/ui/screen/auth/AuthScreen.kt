package com.xinto.mauth.ui.screen.auth

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.BiometricHandler
import com.xinto.mauth.ui.component.pinboard.PinScaffold
import com.xinto.mauth.ui.component.pinboard.rememberPinBoardState
import com.xinto.mauth.ui.component.rememberBiometricHandler
import com.xinto.mauth.ui.component.rememberBiometricPromptData
import org.koin.androidx.compose.getViewModel

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onAuthSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = getViewModel()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val useBiometrics by viewModel.useBiometrics.collectAsStateWithLifecycle()

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
        onNumberAdd = {
            if (viewModel.insertNumber(it)) {
                onAuthSuccess()
            }
        },
        onNumberDelete = viewModel::deleteNumber,
        onClear = viewModel::clear,
        showFingerprint = canUseBiometrics,
        onFingerprintClick = {
            biometricHandler.requestBiometrics(promptData)
        }
    )
}

@Composable
fun AuthScreen(
    code: String,
    onNumberAdd: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onClear: () -> Unit,
    showFingerprint: Boolean,
    onFingerprintClick: () -> Unit,
    modifier: Modifier = Modifier
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
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                }
            )
        },
        codeLength = code.length,
        state = pinBoardState
    )
}