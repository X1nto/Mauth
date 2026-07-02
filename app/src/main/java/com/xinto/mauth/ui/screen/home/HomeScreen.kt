package com.xinto.mauth.ui.screen.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.util.collectAsStateListWithLifecycle
import com.xinto.mauth.ui.util.collectAsStateMapWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreen(
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

    val isExpandedWidth = LocalWindowInfo.current.containerDpSize.width >= 600.dp

    val searchBarState = rememberSearchBarState()
    val searchTextFieldState = rememberTextFieldState()

    val topBarActions: @Composable RowScope.() -> Unit = {
        SortAction(
            activeSortSetting = activeSortSetting,
            onActiveSortChange = onActiveSortChange,
        )
        MoreAction(onMenuNavigate = onMoreMenuNavigate)
    }

    val searchInputField = @Composable {
        SearchInputField(
            searchBarState = searchBarState,
            textFieldState = searchTextFieldState,
            inlineActions = if (isExpandedWidth) null else { -> Row { topBarActions() } }
        )
    }

    LaunchedEffect(searchBarState, searchTextFieldState) {
        snapshotFlow { searchBarState.currentValue }
            .collect { value ->
                if (value == SearchBarValue.Collapsed) {
                    searchTextFieldState.clearText()
                }
            }
    }

    val isSelectionActive = selectedAccounts.isNotEmpty()
    var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    SideEffect {
        if (isSelectionActive) isFabMenuExpanded = false
    }
    BackHandler(enabled = isFabMenuExpanded) {
        isFabMenuExpanded = false
    }
    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = isSelectionActive,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "HomeTopBar",
            ) { selecting ->
                if (selecting) {
                    SelectionTopBar(
                        selectedCount = selectedAccounts.size,
                        onCancelSelection = onCancelAccountSelection,
                        onDeleteSelected = { showDeleteDialog = true },
                        onExportSelected = onExportSelectedAccounts,
                        scrollBehavior = scrollBehavior,
                    )
                } else {
                    AppBarWithSearch(
                        state = searchBarState,
                        inputField = searchInputField,
                        navigationIcon = if (!isExpandedWidth) null else { ->
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        },
                        actions = if (isExpandedWidth) topBarActions else null,
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionActive,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                AccountAddFab(
                    expanded = isFabMenuExpanded,
                    onExpandedChange = { isFabMenuExpanded = it },
                    onAddAccountNavigate = onAddAccountNavigate,
                )
            }
        }
    ) { innerPadding ->
        val contentModifier = remember(innerPadding, scrollBehavior) {
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        }
        when (state) {
            is HomeScreenState.Loading -> {
                Box(
                    modifier = contentModifier,
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is HomeScreenState.Empty -> {
                Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(72.dp),
                        painter = painterResource(R.drawable.ic_empty_dashboard),
                        contentDescription = null
                    )
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        Text(stringResource(R.string.home_dashboard_empty))
                    }
                }
            }
            is HomeScreenState.Success -> {
                LazyVerticalGrid(
                    modifier = contentModifier,
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    columns = GridCells.Adaptive(minSize = 250.dp),
                ) {
                    items(items = state.accounts, key = { it.id }) { account ->
                        val realtimeData = accountRealtimeData[account.id]
                        if (realtimeData != null) {
                            AccountCard(
                                onClick = {
                                    if (selectedAccounts.isNotEmpty()) {
                                        onAccountSelect(account.id)
                                    }
                                },
                                onLongClick = { onAccountSelect(account.id) },
                                onEdit = { onAccountEdit(account.id) },
                                onCounterClick = { onAccountCounterIncrease(account.id) },
                                onCopyCode = { onAccountCopyCode(account.label, realtimeData.code, it) },
                                account = account,
                                realtimeData = realtimeData,
                                selected = selectedAccounts.contains(account.id),
                                colors = CardDefaults.elevatedCardColors(),
                                elevation = CardDefaults.elevatedCardElevation()
                            )
                        }
                    }
                }
            }
            is HomeScreenState.Error -> {
                Column(
                    modifier = contentModifier,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        contentDescription = null
                    )
                    Text(stringResource(R.string.home_dashboard_error))
                }
            }
        }
    }

    val searchResults: @Composable ColumnScope.() -> Unit = {
        SearchResults(
            state = state,
            searchTextFieldState = searchTextFieldState,
            onAccountEdit = onAccountEdit,
            onAccountCounterIncrease = onAccountCounterIncrease,
            onAccountCopyCode = onAccountCopyCode,
            selectedAccounts = selectedAccounts,
            accountRealtimeData = accountRealtimeData,
        )
    }
    if (isExpandedWidth) {
        ExpandedDockedSearchBar(
            state = searchBarState,
            inputField = searchInputField,
            content = searchResults
        )
    } else {
        ExpandedFullScreenSearchBar(
            state = searchBarState,
            inputField = searchInputField,
            content = searchResults
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchInputField(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    inlineActions: (@Composable () -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val expanded = searchBarState.targetValue == SearchBarValue.Expanded
    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = { keyboardController?.hide() },
        placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
        leadingIcon = {
            if (expanded) {
                IconButton(onClick = { coroutineScope.launch { searchBarState.animateToCollapsed() } }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.home_search_collapse),
                    )
                }
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                )
            }
        },
        trailingIcon = {
            if (expanded) {
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(onClick = { textFieldState.clearText() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.home_search_clear),
                        )
                    }
                }
            } else {
                inlineActions?.invoke()
            }
        }
    )
}

@Composable
private fun SortAction(
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit,
) {
    var isSortVisible by remember { mutableStateOf(false) }
    IconButton(onClick = { isSortVisible = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_sort),
            contentDescription = null,
        )
        DropdownMenu(
            expanded = isSortVisible,
            onDismissRequest = { isSortVisible = false },
        ) {
            SortSetting.entries.forEach { sortSetting ->
                DropdownMenuItem(
                    onClick = {
                        isSortVisible = false
                        onActiveSortChange(sortSetting)
                    },
                    text = {
                        val resource = when (sortSetting) {
                            SortSetting.DateAsc -> R.string.home_sort_date_ascending
                            SortSetting.DateDesc -> R.string.home_sort_date_descending
                            SortSetting.LabelAsc -> R.string.home_sort_label_ascending
                            SortSetting.LabelDesc -> R.string.home_sort_label_descending
                            SortSetting.IssuerAsc -> R.string.home_sort_issuer_ascending
                            SortSetting.IssuerDesc -> R.string.home_sort_issuer_descending
                        }
                        Text(stringResource(resource))
                    },
                    trailingIcon = {
                        if (activeSortSetting == sortSetting) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MoreAction(onMenuNavigate: (HomeMoreMenu) -> Unit) {
    var isMoreVisible by remember { mutableStateOf(false) }
    IconButton(onClick = { isMoreVisible = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = null,
        )
        DropdownMenu(
            expanded = isMoreVisible,
            onDismissRequest = { isMoreVisible = false },
        ) {
            HomeMoreMenu.entries.forEach { menu ->
                DropdownMenuItem(
                    text = { Text(stringResource(menu.title)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(menu.icon),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        isMoreVisible = false
                        onMenuNavigate(menu)
                    },
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SearchResults(
    state: HomeScreenState,
    searchTextFieldState: TextFieldState,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    selectedAccounts: SnapshotStateList<UUID>,
    accountRealtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
) {
    val allAccounts = (state as? HomeScreenState.Success)?.accounts
    val query = searchTextFieldState.text.toString().trim()
    val filteredAccounts = remember(allAccounts, query) {
        when {
            allAccounts == null -> persistentListOf()
            query.isEmpty() -> allAccounts
            else -> allAccounts
                .filter { it.label.contains(query, ignoreCase = true) || it.issuer.contains(query, ignoreCase = true) }
                .toImmutableList()
        }
    }
    if (filteredAccounts.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = filteredAccounts, key = { it.id }) { account ->
                val realtimeData = accountRealtimeData[account.id]
                if (realtimeData != null) {
                    AccountCard(
                        onClick = {},
                        onLongClick = {},
                        onEdit = { onAccountEdit(account.id) },
                        onCounterClick = { onAccountCounterIncrease(account.id) },
                        onCopyCode = { onAccountCopyCode(account.label, realtimeData.code, it) },
                        account = account,
                        realtimeData = realtimeData,
                        selected = selectedAccounts.contains(account.id),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    )
                }
            }
        }
    } else if (query.isNotEmpty()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.home_search_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExportSelected: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onCancelSelection) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.home_selection_clear),
                )
            }
        },
        title = { Text(pluralStringResource(R.plurals.home_selection_count, selectedCount, selectedCount)) },
        actions = {
            IconButton(onClick = onDeleteSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever),
                    contentDescription = null,
                )
            }
            IconButton(onClick = onExportSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_export),
                    contentDescription = null,
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountAddFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddAccountNavigate: (HomeAddAccountMenu) -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = onExpandedChange,
            ) {
                Icon(
                    modifier = Modifier
                        .rotate(checkedProgress * 45f)
                        .animateIcon(checkedProgress = { checkedProgress }),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.home_addaccount_title),
                )
            }
        },
    ) {
        HomeAddAccountMenu.entries.forEach { menu ->
            FloatingActionButtonMenuItem(
                onClick = {
                    onExpandedChange(false)
                    onAddAccountNavigate(menu)
                },
                text = { Text(stringResource(menu.title)) },
                icon = {
                    Icon(
                        painter = painterResource(menu.icon),
                        contentDescription = null,
                    )
                },
            )
        }
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