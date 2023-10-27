package com.xinto.mauth.ui.screen.pinremove

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.pinboard.PinScaffold
import com.xinto.mauth.ui.component.pinboard.rememberPinBoardState
import org.koin.androidx.compose.getViewModel

@Composable
fun PinRemoveScreen(
    onExit: () -> Unit
) {
    val viewModel: PinRemoveViewModel = getViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onExit)
    PinRemoveScreen(
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

@Composable
fun PinRemoveScreen(
    state: PinRemoveScreenState,
    onEnter: () -> Unit,
    onBack: () -> Unit,
    onNumberEnter: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onAllDelete: () -> Unit,
) {
    PinScaffold(
        codeLength = state.code.length,
        error = state is PinRemoveScreenState.Error,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.pinremove_title))
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