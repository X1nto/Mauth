package com.xinto.mauth.ui.screen.account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.R.drawable.ic_error
import com.xinto.mauth.R.string.account_error
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.ui.component.form.form
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun AddAccountScreen(
    prefilled: DomainAccountInfo,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AccountViewModel = koinViewModel {
        parametersOf(AccountViewModelParams.Prefilled(prefilled))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountScreen(
        modifier = modifier,
        title = stringResource(R.string.account_title_add),
        state = state,
        onSave = {
            val account = (state as? AccountScreenState.Success)?.form?.validate()
            if (account != null) {
                viewModel.saveData(account)
                onExit()
            }
        },
        onExit = onExit
    )
}

@Composable
fun EditAccountScreen(
    id: UUID,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AccountViewModel = koinViewModel {
        parametersOf(AccountViewModelParams.Id(id))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    AccountScreen(
        modifier = modifier,
        title = stringResource(R.string.account_title_edit),
        state = state,
        onSave = {
            val account = (state as? AccountScreenState.Success)?.form?.validate()
            if (account != null) {
                viewModel.saveData(account)
                onExit()
            }
        },
        onExit = onExit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    title: String,
    state: AccountScreenState,
    onSave: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExitDialogShown by remember { mutableStateOf(false) }
    val hasChanges by remember(state) {
        derivedStateOf {
            state is AccountScreenState.Success && !state.form.isSame()
        }
    }
    BackHandler {
        if (hasChanges) {
            isExitDialogShown = true
        } else {
            onExit()
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = state is AccountScreenState.Success
                    ) {
                        Text(stringResource(R.string.account_action_save))
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
                title = { Text(title) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentAlignment = Alignment.TopCenter
        ) {
            when (state) {
                is AccountScreenState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AccountScreenState.Success -> {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp),
                        columns = Fixed(2)
                    ) {
                        form(lazyGridForm = state.form)
                    }
                }
                is AccountScreenState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(ic_error),
                            contentDescription = null
                        )
                        Text(stringResource(account_error))
                    }
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

@Composable
private fun AccountExitDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.account_discard_title)) },
        text = { Text(stringResource(R.string.account_discard_subtitle)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.account_discard_buttons_discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.account_discard_buttons_cancel))
            }
        }
    )
}

@Composable
@PreviewAllConfigurations
private fun AccountScreen_Loading_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AccountScreen(
                modifier = Modifier.fillMaxSize(),
                title = "Add account",
                state = AccountScreenState.Loading,
                onSave = {},
                onExit = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AccountScreen_Success_Preview() {
    val form = remember {
        AccountForm(
            initial = DomainAccountInfo(
                id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                icon = null,
                label = "Google",
                issuer = "google.com",
                secret = "JBSWY3DPEHPK3PXP",
                algorithm = OtpDigest.SHA1,
                type = OtpType.TOTP,
                digits = 6,
                counter = 0,
                period = 30,
                groupId = null,
                createdMillis = 0L
            ),
            groups = MutableStateFlow(emptyList()),
            onCreateGroup = { _, _ -> UUID.randomUUID() }
        )
    }
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AccountScreen(
                modifier = Modifier.fillMaxSize(),
                title = "Add account",
                state = AccountScreenState.Success(form),
                onSave = {},
                onExit = {}
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun AccountScreen_Error_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AccountScreen(
                modifier = Modifier.fillMaxSize(),
                title = "Edit account",
                state = AccountScreenState.Error(error = "Failed to load account"),
                onSave = {},
                onExit = {}
            )
        }
    }
}