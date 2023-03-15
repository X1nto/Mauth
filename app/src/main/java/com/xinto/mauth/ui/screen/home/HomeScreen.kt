package com.xinto.mauth.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.screen.home.component.HomeAddAccountSheet
import com.xinto.mauth.ui.screen.home.component.HomeBottomBar
import com.xinto.mauth.ui.screen.home.component.HomeDeleteAccountsDialog
import com.xinto.mauth.ui.screen.home.state.HomeScreenEmpty
import com.xinto.mauth.ui.screen.home.state.HomeScreenError
import com.xinto.mauth.ui.screen.home.state.HomeScreenLoading
import com.xinto.mauth.ui.screen.home.state.HomeScreenSuccess
import org.koin.androidx.compose.koinViewModel
import java.util.*

@Composable
fun HomeScreen(
    onAddAccountManually: () -> Unit,
    onAddAccountViaScanning: () -> Unit,
    onAddAccountFromImage: (DomainAccountInfo) -> Unit,
    onSettingsClick: () -> Unit,
    onAccountEdit: (UUID) -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.getAccountInfoFromQrUri(uri)?.let {
            onAddAccountFromImage(it)
        }
    }
    HomeScreen(
        onAddAccountManually = onAddAccountManually,
        onAddAccountViaScanning = onAddAccountViaScanning,
        onAddAccountFromImage = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
        onSettingsClick = onSettingsClick,
        onAccountSelect = viewModel::toggleAccountSelection,
        onCancelAccountSelection = viewModel::clearAccountSelection,
        onDeleteSelectedAccounts = viewModel::deleteSelectedAccounts,
        onAccountEdit = onAccountEdit,
        onAccountCounterIncrease = viewModel::incrementCounter,
        onAccountCopyCode = viewModel::copyCodeToClipboard,
        state = viewModel.state,
        accountRealtimeData = viewModel.realtimeData,
        selectedAccounts = viewModel.selectedAccounts
    )
}

@Composable
fun HomeScreen(
    onAddAccountManually: () -> Unit,
    onAddAccountViaScanning: () -> Unit,
    onAddAccountFromImage: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountSelect: (UUID) -> Unit,
    onCancelAccountSelection: () -> Unit,
    onDeleteSelectedAccounts: () -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String) -> Unit,
    state: HomeScreenState,
    accountRealtimeData: Map<UUID, DomainOtpRealtimeData>,
    selectedAccounts: List<UUID>,
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                }
            )
        },
        bottomBar = {
            HomeBottomBar(
                isSelectionActive = selectedAccounts.isNotEmpty(),
                onAdd = {
                    showAddSheet = true
                },
                onCancelSelection = onCancelAccountSelection,
                onDeleteSelected = {
                    showDeleteDialog = true
                },
                onSettingsClick = onSettingsClick
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            when (state) {
                is HomeScreenState.Loading -> {
                    HomeScreenLoading()
                }
                is HomeScreenState.Empty -> {
                    HomeScreenEmpty()
                }
                is HomeScreenState.Success -> {
                    HomeScreenSuccess(
                        onAccountSelect = onAccountSelect,
                        onAccountEdit = onAccountEdit,
                        onAccountCounterIncrease = onAccountCounterIncrease,
                        onAccountCopyCode = onAccountCopyCode,
                        accounts = state.accounts,
                        selectedAccounts = selectedAccounts,
                        accountRealtimeData = accountRealtimeData
                    )
                }
                is HomeScreenState.Error -> {
                    HomeScreenError()
                }
            }
        }
    }
    if (showAddSheet) {
        HomeAddAccountSheet(
            onDismiss = {
                showAddSheet = false
            },
            onManualEnterClick = {
                showAddSheet = false
                onAddAccountManually()
            },
            onScanQrClick = {
                showAddSheet = false
                onAddAccountViaScanning()
            },
            onChooseImage = {
                showAddSheet = false
                onAddAccountFromImage()
            },
        )
    }
    if (showDeleteDialog) {
        HomeDeleteAccountsDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteSelectedAccounts()
            },
            onCancel = {
                showDeleteDialog = false
            }
        )
    }
}