package com.xinto.mauth.ui.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.screen.home.component.HomeAddAccountSheet
import com.xinto.mauth.ui.screen.home.component.HomeDeleteAccountsDialog
import com.xinto.mauth.ui.screen.home.component.HomeScaffold
import com.xinto.mauth.ui.screen.home.state.HomeScreenEmpty
import com.xinto.mauth.ui.screen.home.state.HomeScreenError
import com.xinto.mauth.ui.screen.home.state.HomeScreenLoading
import com.xinto.mauth.ui.screen.home.state.HomeScreenSuccess
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun HomeScreen(
    onAddAccountManually: () -> Unit,
    onAddAccountViaScanning: () -> Unit,
    onAddAccountFromImage: (DomainAccountInfo) -> Unit,
    onMenuNavigate: (HomeMoreMenu) -> Unit,
    onAccountEdit: (UUID) -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val realTimeData by viewModel.realTimeData.collectAsStateWithLifecycle()
    val selectedAccounts by viewModel.selectedAccounts.collectAsStateWithLifecycle()
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
        onMenuNavigate = onMenuNavigate,
        onAccountSelect = viewModel::toggleAccountSelection,
        onCancelAccountSelection = viewModel::clearAccountSelection,
        onDeleteSelectedAccounts = viewModel::deleteSelectedAccounts,
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

@Composable
fun HomeScreen(
    onAddAccountNavigate: (HomeAddAccountMenu) -> Unit,
    onMenuNavigate: (HomeMoreMenu) -> Unit,
    onAccountSelect: (UUID) -> Unit,
    onCancelAccountSelection: () -> Unit,
    onDeleteSelectedAccounts: () -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    state: HomeScreenState,
    accountRealtimeData: Map<UUID, DomainOtpRealtimeData>,
    selectedAccounts: List<UUID>,
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    HomeScaffold(
        isSelectionActive = selectedAccounts.isNotEmpty(),
        onAdd = {
            showAddSheet = true
        },
        onCancelSelection = onCancelAccountSelection,
        onDeleteSelected = {
            showDeleteDialog = true
        },
        onMenuNavigate = onMenuNavigate,
        activeSortSetting = activeSortSetting,
        onActiveSortChange = onActiveSortChange,
        scrollBehavior = scrollBehavior
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
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
            onAddAccountNavigate = {
                showAddSheet = false
                onAddAccountNavigate(it)
            }
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