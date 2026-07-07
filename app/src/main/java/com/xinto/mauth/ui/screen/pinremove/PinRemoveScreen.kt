package com.xinto.mauth.ui.screen.pinremove

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
fun PinRemoveScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PinRemoveViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    PinRemoveScreen(
        modifier = modifier,
        state = state,
        onEnter = {
            if (viewModel.removePin()) {
                onExit()
            }
        },
        onBack = onExit,
        onNumberEnter = viewModel::addNumber,
        onNumberDelete = viewModel::deleteLast,
        onAllDelete = viewModel::clear
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinRemoveScreen(
    state: PinRemoveScreenState,
    onEnter: () -> Unit,
    onBack: () -> Unit,
    onNumberEnter: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onAllDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PinScaffold(
        modifier = modifier,
        codeLength = state.code.length,
        error = state is PinRemoveScreenState.Error,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pinremove_title)) },
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
        description = null,
        state = rememberPinBoardState(
            showEnter = true,
            onNumberClick = onNumberEnter,
            onBackspaceClick = onNumberDelete,
            onEnterClick = onEnter,
            onBackspaceLongClick = onAllDelete
        )
    )
}

@Composable
@PreviewAllConfigurations
private fun PinRemoveScreen_Stale_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinRemoveScreen(
                modifier = Modifier.fillMaxSize(),
                state = PinRemoveScreenState.Stale("123"),
                onEnter = {},
                onBack = {},
                onNumberEnter = {},
                onNumberDelete = {},
                onAllDelete = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun PinRemoveScreen_Error_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PinRemoveScreen(
                modifier = Modifier.fillMaxSize(),
                state = PinRemoveScreenState.Error,
                onEnter = {},
                onBack = {},
                onNumberEnter = {},
                onNumberDelete = {},
                onAllDelete = {}
            )
        }
    }
}