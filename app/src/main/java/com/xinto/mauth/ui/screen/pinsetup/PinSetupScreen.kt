package com.xinto.mauth.ui.screen.pinsetup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.ui.component.pinboard.PinScaffold
import com.xinto.mauth.ui.component.pinboard.rememberPinBoardState
import org.koin.androidx.compose.getViewModel

@Composable
fun PinSetupScreen(
    onExit: () -> Unit
) {
    val viewModel: PinSetupViewModel = getViewModel()
    val code by viewModel.code.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    PinSetupScreen(
        code = code,
        state = state,
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

@Composable
fun PinSetupScreen(
    code: String,
    state: PinSetupScreenState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onNumberEnter: (Char) -> Unit,
    onNumberDelete: () -> Unit,
    onAllDelete: () -> Unit,
) {
    BackHandler(onBack = onPrevious)
    PinScaffold(
        codeLength = code.length,
        topBar = {
            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = state,
                        label = "PinSetupDescription",
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        }
                    ) {
                        val resource = when (it) {
                            is PinSetupScreenState.Initial -> R.string.pinsetup_description_create
                            is PinSetupScreenState.Confirm -> R.string.pinsetup_description_confirm
                        }
                        Text(stringResource(resource))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
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
            onFingerprintClick = onNumberDelete,
            onBackspaceClick = onNumberDelete,
            onEnterClick = onNext,
            onBackspaceLongClick = onAllDelete
        )
    )
}