package com.xinto.mauth.ui.screen.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.screen.account.component.AccountExitDialog
import com.xinto.mauth.ui.screen.account.state.AccountScreenError
import com.xinto.mauth.ui.screen.account.state.AccountScreenLoading
import com.xinto.mauth.ui.screen.account.state.AccountScreenSuccess
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.*

@Composable
fun AddAccountScreen(
    prefilled: DomainAccountInfo,
    onExit: () -> Unit
) {
    val viewModel: AccountViewModel = koinViewModel {
        parametersOf(AccountViewModelParams.Prefilled(prefilled))
    }
    AccountScreen(
        title = stringResource(R.string.addeditaccount_title_add),
        state = viewModel.state,
        onSave = {
            viewModel.saveData(it)
            onExit()
        },
        onExit = onExit
    )
}

@Composable
fun EditAccountScreen(
    id: UUID,
    onExit: () -> Unit
) {
    val viewModel: AccountViewModel = koinViewModel {
        parametersOf(AccountViewModelParams.Id(id))
    }
    AccountScreen(
        title = stringResource(R.string.addeditaccount_title_edit),
        state = viewModel.state,
        onSave = {
            viewModel.saveData(it)
            onExit()
        },
        onExit = onExit
    )
}

@Composable
fun AccountScreen(
    title: String,
    state: AccountScreenState,
    onSave: (DomainAccountInfo) -> Unit,
    onExit: () -> Unit,
) {
    var isExitDialogShown by remember { mutableStateOf(false) }
    var accountInfo: DomainAccountInfo? by rememberSaveable {
        mutableStateOf(null)
    }
    LaunchedEffect(state) {
        if (state is AccountScreenState.Success) {
            accountInfo = state.info
        }
    }
    BackHandler {
        if (state is AccountScreenState.Success) {
            isExitDialogShown = true
        } else {
            onExit()
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    Button(
                        onClick = {
                            onSave(accountInfo!!)
                        },
                        enabled = accountInfo != null
                    ) {
                        Text(stringResource(R.string.addeditaccount_actions_save))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (accountInfo != null) {
                            isExitDialogShown = true
                        } else {
                            onExit()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(title)
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            when (state) {
                is AccountScreenState.Loading -> {
                    AccountScreenLoading()
                }
                is AccountScreenState.Success -> {
                    accountInfo?.let { info ->
                        AccountScreenSuccess(
                            id = info.id,
                            icon = info.icon,
                            onIconChange = { accountInfo = info.copy(icon = it) },
                            label = info.label,
                            onLabelChange = { accountInfo = info.copy(label = it) },
                            issuer = info.issuer,
                            onIssuerChange = { accountInfo = info.copy(issuer = it) },
                            secret = info.secret,
                            onSecretChange = { accountInfo = info.copy(secret = it) },
                            type = info.type,
                            onTypeChange = { accountInfo = info.copy(type = it) },
                            digest = info.algorithm,
                            onDigestChange = { accountInfo = info.copy(algorithm = it) },
                            digits = info.digits,
                            onDigitsChange = { accountInfo = info.copy(digits = it) },
                            counter = info.counter,
                            onCounterChange = { accountInfo = info.copy(counter = it) },
                            period = info.period,
                            onPeriodChange = { accountInfo = info.copy(period = it) }
                        )
                    }
                }
                is AccountScreenState.Error -> {
                    AccountScreenError()
                }
            }
        }
    }
    if (isExitDialogShown) {
        AccountExitDialog(
            onCancel = {
                isExitDialogShown = false
            },
            onConfirm = {
                isExitDialogShown = false
                onExit()
            }
        )
    }
}