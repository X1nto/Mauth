package com.xinto.mauth.ui.screen.pinsetup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.pinboard.PinScaffold
import com.xinto.mauth.ui.component.pinboard.rememberPinBoardState
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun PinSetupScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PinSetupViewModel = koinViewModel()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    BackHandler(onBack = {
        if (viewModel.previous()) {
            onExit()
        }
    })
    PinSetupScreen(
        modifier = modifier,
        code = code,
        state = state,
        error = error,
        onNext = {
            if (viewModel.next()) {
                onExit()
            }
        },
        onPrevious = {
            if (viewModel.previous()) {
                onExit()
            }
        },
        onNumberEnter = viewModel::addNumber,
        onNumberDelete = viewModel::deleteLast,
        onAllDelete = viewModel::clear
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    code: String,
    state: PinSetupScreenState,
    error: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onNumberEnter: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onAllDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PinScaffold(
        modifier = modifier,
        codeLength = code.length,
        error = error,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = state,
                        label = "PinSetupDescription",
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) {
                        val resource = when (it) {
                            is PinSetupScreenState.Initial -> R.string.pinsetup_title_create
                            is PinSetupScreenState.Confirm -> R.string.pinsetup_title_confirm
                        }
                        Text(stringResource(resource))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPrevious) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        description = null,
        state = rememberPinBoardState(
            showEnter = true,
            onNumberClick = onNumberEnter,
            onBackspaceClick = onNumberDelete,
            onEnterClick = onNext,
            onBackspaceLongClick = onAllDelete
        )
    )
}

@Composable
@PreviewAllConfigurations
private fun PinSetupScreen_Initial_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinSetupScreen(
                modifier = Modifier.fillMaxSize(),
                code = "12",
                state = PinSetupScreenState.Initial,
                error = false,
                onNext = {},
                onPrevious = {},
                onNumberEnter = {},
                onNumberDelete = {},
                onAllDelete = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun PinSetupScreen_Confirm_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinSetupScreen(
                modifier = Modifier.fillMaxSize(),
                code = "1234",
                state = PinSetupScreenState.Confirm,
                error = false,
                onNext = {},
                onPrevious = {},
                onNumberEnter = {},
                onNumberDelete = {},
                onAllDelete = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun PinSetupScreen_ConfirmError_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinSetupScreen(
                modifier = Modifier.fillMaxSize(),
                code = "1234",
                state = PinSetupScreenState.Confirm,
                error = true,
                onNext = {},
                onPrevious = {},
                onNumberEnter = {},
                onNumberDelete = {},
                onAllDelete = {}
            )
        }
    }
}