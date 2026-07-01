package com.xinto.mauth.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.screen.home.component.HomeScaffold
import com.xinto.mauth.ui.screen.home.state.HomeScreenEmpty
import com.xinto.mauth.ui.screen.home.state.HomeScreenError
import com.xinto.mauth.ui.screen.home.state.HomeScreenLoading
import com.xinto.mauth.ui.screen.home.state.HomeScreenSuccess
import com.xinto.mauth.ui.util.collectAsStateListWithLifecycle
import com.xinto.mauth.ui.util.collectAsStateMapWithLifecycle
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun HomeScreen(
    onAddAccountManually: () -> Unit,
    onAddAccountViaScanning: () -> Unit,
    onAddAccountFromImage: (DomainAccountInfo) -> Unit,
    onSettingsNavigate: () -> Unit,
    onExportNavigate: (accounts: List<UUID>) -> Unit,
    onAboutNavigate: () -> Unit,
    onAccountEdit: (UUID) -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val realTimeData = viewModel.realTimeData.collectAsStateMapWithLifecycle()
    val selectedAccounts = viewModel.selectedAccounts.collectAsStateListWithLifecycle()
    val activeSortSetting by viewModel.activeSortSetting.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.getAccountInfoFromQrUri(uri)?.let {
            onAddAccountFromImage(it)
        }
    }

    HomeScreen(
        onAddAccountNavigate = {
            when (it) {
                HomeAddAccountMenu.ScanQR -> onAddAccountViaScanning()
                HomeAddAccountMenu.ImageQR -> {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
                HomeAddAccountMenu.Manual -> onAddAccountManually()
            }
        },
        onMoreMenuNavigate = {
            when (it) {
                HomeMoreMenu.Settings -> onSettingsNavigate()
                HomeMoreMenu.Export -> onExportNavigate(selectedAccounts)
                HomeMoreMenu.About -> onAboutNavigate()
            }
        },
        onAccountSelect = viewModel::toggleAccountSelection,
        onCancelAccountSelection = viewModel::clearAccountSelection,
        onDeleteSelectedAccounts = viewModel::deleteSelectedAccounts,
        onExportSelectedAccounts = { onExportNavigate(selectedAccounts) },
        onAccountEdit = onAccountEdit,
        onAccountCounterIncrease = viewModel::incrementCounter,
        onAccountCopyCode = viewModel::copyCodeToClipboard,
        state = state,
        accountRealtimeData = realTimeData,
        selectedAccounts = selectedAccounts,
        activeSortSetting = activeSortSetting,
        onActiveSortChange = viewModel::setActiveSort,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddAccountNavigate: (HomeAddAccountMenu) -> Unit,
    onMoreMenuNavigate: (HomeMoreMenu) -> Unit,
    onAccountSelect: (UUID) -> Unit,
    onCancelAccountSelection: () -> Unit,
    onDeleteSelectedAccounts: () -> Unit,
    onExportSelectedAccounts: () -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    state: HomeScreenState,
    accountRealtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
    selectedAccounts: SnapshotStateList<UUID>,
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    HomeScaffold(
        isSelectionActive = selectedAccounts.isNotEmpty(),
        selectedCount = selectedAccounts.size,
        onAddAccountNavigate = onAddAccountNavigate,
        onCancelSelection = onCancelAccountSelection,
        onDeleteSelected = { showDeleteDialog = true },
        onExportSelected = onExportSelectedAccounts,
        onMenuNavigate = onMoreMenuNavigate,
        activeSortSetting = activeSortSetting,
        onActiveSortChange = onActiveSortChange,
        scrollBehavior = scrollBehavior
    ) {
        val modifier = remember(it, scrollBehavior) {
            Modifier
                .fillMaxSize()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        }
        when (state) {
            is HomeScreenState.Loading -> {
                HomeScreenLoading(modifier)
            }
            is HomeScreenState.Empty -> {
                HomeScreenEmpty(modifier)
            }
            is HomeScreenState.Success -> {
                HomeScreenSuccess(
                    modifier = modifier,
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
                HomeScreenError(modifier)
            }
        }
    }
    if (showDeleteDialog) {
        DeleteDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteSelectedAccounts()
            },
            onDismissRequest = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_delete_forever),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.home_delete_title)) },
        text = { Text(stringResource(R.string.home_delete_subtitle)) },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text(stringResource(R.string.home_delete_button_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.home_delete_button_cancel))
            }
        }
    )
}