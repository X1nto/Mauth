package com.xinto.mauth.ui.screen.auth

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
fun AuthScreen(modifier: Modifier = Modifier) {
    val viewModel: AuthViewModel = getViewModel()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val biometricHandler = rememberBiometricHandler(
        onAuthSuccess = {  },
    )
    AuthScreen(
        modifier = modifier,
        code = code,
        biometricHandler = biometricHandler,
        onNumberAdd = {
            if (viewModel.insertNumber(it)) {

            }
        },
        onNumberDelete = viewModel::deleteNumber,
        onClear = viewModel::clear
    )
}

@Composable
fun AuthScreen(
    code: String,
    biometricHandler: BiometricHandler,
    onNumberAdd: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val biometricPrompt = rememberBiometricPromptData(
        title = "Test",
        negativeButtonText = "Cancel",
    )
    val pinBoardState = rememberPinBoardState(
        showFingerprint = biometricHandler.canUseBiometrics(),
        onFingerprintClick = {
            biometricHandler.requestBiometrics(biometricPrompt)
        },
        onNumberClick = onNumberAdd,
        onBackspaceClick = onNumberDelete,
        onBackspaceLongClick = onClear
    )
    DisposableEffect(biometricHandler) {
        if (biometricHandler.canUseBiometrics()) {
            biometricHandler.requestBiometrics(biometricPrompt)
        }

        onDispose {
            biometricHandler.cancelRequest()
        }
    }
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