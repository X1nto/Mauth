package com.xinto.mauth.ui.screen.account

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.screen.account.component.AccountExitDialog
import com.xinto.mauth.ui.screen.account.state.AccountScreenError
import com.xinto.mauth.ui.screen.account.state.AccountScreenLoading
import com.xinto.mauth.ui.screen.account.state.AccountScreenSuccess
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun AddAccountScreen(
    prefilled: DomainAccountInfo,
    onExit: () -> Unit
) {
    val viewModel: AccountViewModel = koinViewModel {
        parametersOf(AccountViewModelParams.Prefilled(prefilled))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle()
    val canSave by viewModel.canSave.collectAsStateWithLifecycle()
    AccountScreen(
        title = stringResource(R.string.account_title_add),
        state = state,
        hasChanges = hasChanges,
        canSave = canSave,
        onIconChange = viewModel::updateIcon,
        onLabelChange = viewModel::updateLabel,
        onIssuerChange = viewModel::updateIssuer,
        onSecretChange = viewModel::updateSecret,
        onTypeChange = viewModel::updateType,
        onDigestChange = viewModel::updateDigest,
        onDigitsChange = viewModel::updateDigits,
        onCounterChange = viewModel::updateCounter,
        onPeriodChange = viewModel::updatePeriod,
        onSave = {
            viewModel.saveData()
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle()
    val canSave by viewModel.canSave.collectAsStateWithLifecycle()
    AccountScreen(
        title = stringResource(R.string.account_title_edit),
        state = state,
        hasChanges = hasChanges,
        canSave = canSave,
        onIconChange = viewModel::updateIcon,
        onLabelChange = viewModel::updateLabel,
        onIssuerChange = viewModel::updateIssuer,
        onSecretChange = viewModel::updateSecret,
        onTypeChange = viewModel::updateType,
        onDigestChange = viewModel::updateDigest,
        onDigitsChange = viewModel::updateDigits,
        onCounterChange = viewModel::updateCounter,
        onPeriodChange = viewModel::updatePeriod,
        onSave = {
            viewModel.saveData()
            onExit()
        },
        onExit = onExit
    )
}

@Composable
fun AccountScreen(
    title: String,
    state: AccountScreenState,
    hasChanges: Boolean,
    canSave: Boolean,
    onIconChange: (Uri?) -> Unit,
    onLabelChange: (String) -> Unit,
    onIssuerChange: (String) -> Unit,
    onSecretChange: (String) -> Unit,
    onTypeChange: (OtpType) -> Unit,
    onDigestChange: (OtpDigest) -> Unit,
    onDigitsChange: (String) -> Unit,
    onCounterChange: (String) -> Unit,
    onPeriodChange: (String) -> Unit,
    onSave: () -> Unit,
    onExit: () -> Unit,
) {
    var isExitDialogShown by remember { mutableStateOf(false) }
    BackHandler {
        if (hasChanges) {
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
                    TextButton(
                        onClick = onSave,
                        enabled = canSave
                    ) {
                        Text(stringResource(R.string.account_actions_save))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            isExitDialogShown = true
                        } else {
                            onExit()
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
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
                    AccountScreenSuccess(
                        info = state.info,
                        onIconChange = onIconChange,
                        onLabelChange = onLabelChange,
                        onIssuerChange = onIssuerChange,
                        onSecretChange = onSecretChange,
                        onTypeChange = onTypeChange,
                        onDigestChange = onDigestChange,
                        onDigitsChange = onDigitsChange,
                        onCounterChange = onCounterChange,
                        onPeriodChange = onPeriodChange
                    )
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