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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.domain.group.model.GroupFilter
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.screen.groups.CreateGroupDialog
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.ui.util.collectAsStateListWithLifecycle
import com.xinto.mauth.ui.util.collectAsStateMapWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun HomeScreen(
    onAddAccountManually: (groupId: UUID?) -> Unit,
    onAddAccountViaScanning: () -> Unit,
    onAddAccountFromImage: (DomainAccountInfo) -> Unit,
    onSettingsNavigate: () -> Unit,
    onExportNavigate: (accounts: List<UUID>) -> Unit,
    onAboutNavigate: () -> Unit,
    onAccountEdit: (UUID) -> Unit,
    onManageGroups: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val realTimeData = viewModel.realTimeData.collectAsStateMapWithLifecycle()
    val selectedAccounts = viewModel.selectedAccounts.collectAsStateListWithLifecycle()
    val activeSortSetting by viewModel.activeSortSetting.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val activeGroup by viewModel.activeGroup.collectAsStateWithLifecycle()
    val searchAccounts by viewModel.searchAccounts.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.getAccountInfoFromQrUri(uri)?.let {
            onAddAccountFromImage(it)
        }
    }

    var showGroupCreateDialog by remember { mutableStateOf(false) }
    if (showGroupCreateDialog) {
        CreateGroupDialog(
            isNameTaken = { candidate -> groups.any { it.name.equals(candidate, ignoreCase = true) } },
            onConfirm = { name, emoji ->
                viewModel.createGroup(name, emoji)
                showGroupCreateDialog = false
            },
            onDismissRequest = { showGroupCreateDialog = false }
        )
    }

    var showMoveSheet by remember { mutableStateOf(false) }
    var showMoveCreateDialog by remember { mutableStateOf(false) }
    if (showMoveSheet) {
        MoveToGroupSheet(
            groups = groups,
            onSelectGroup = { groupId ->
                viewModel.moveSelectedToGroup(groupId)
                showMoveSheet = false
            },
            onCreateGroup = { showMoveCreateDialog = true },
            onDismiss = { showMoveSheet = false }
        )
    }
    if (showMoveCreateDialog) {
        CreateGroupDialog(
            isNameTaken = { candidate -> groups.any { it.name.equals(candidate, ignoreCase = true) } },
            onConfirm = { name, emoji ->
                viewModel.createGroupAndMoveSelected(name, emoji)
                showMoveCreateDialog = false
                showMoveSheet = false
            },
            onDismissRequest = { showMoveCreateDialog = false }
        )
    }

    HomeScreen(
        modifier = modifier,
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
                HomeAddAccountMenu.Manual -> onAddAccountManually((activeGroup as? GroupFilter.Specific)?.id)
            }
        },
        onMoreMenuNavigate = {
            when (it) {
                HomeMoreMenu.Groups -> onManageGroups()
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
        groups = groups,
        activeGroup = activeGroup,
        onActiveGroupChange = viewModel::setActiveGroup,
        onCreateGroupClick = { showGroupCreateDialog = true },
        onGroupSelectedClick = { showMoveSheet = true },
        searchAccounts = searchAccounts,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    onActiveSortChange: (SortSetting) -> Unit,
    groups: ImmutableList<DomainGroup>,
    activeGroup: GroupFilter,
    onActiveGroupChange: (GroupFilter) -> Unit,
    onCreateGroupClick: () -> Unit,
    onGroupSelectedClick: () -> Unit,
    searchAccounts: ImmutableList<DomainAccount>,
    modifier: Modifier = Modifier
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
        modifier = modifier,
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
                        canMoveToGroup = groups.isNotEmpty(),
                        onMoveToGroup = onGroupSelectedClick,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (groups.isNotEmpty()) {
                AnimatedVisibility(visible = !isSelectionActive) {
                    GroupFilterRow(
                        modifier = Modifier.padding(top = 8.dp),
                        groups = groups,
                        activeGroup = activeGroup,
                        onActiveGroupChange = onActiveGroupChange,
                        onAddGroup = onCreateGroupClick,
                    )
                }
            }
            val contentModifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
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
                    val emptyMessage = when (activeGroup) {
                        GroupFilter.All -> R.string.home_dashboard_empty
                        GroupFilter.Ungrouped, is GroupFilter.Specific -> R.string.home_dashboard_empty_group
                    }
                    val emptyIcon = when (activeGroup) {
                        GroupFilter.All -> R.drawable.ic_empty_dashboard
                        GroupFilter.Ungrouped, is GroupFilter.Specific -> R.drawable.ic_label_off
                    }
                    Column(
                        modifier = contentModifier,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(72.dp),
                            painter = painterResource(emptyIcon),
                            contentDescription = null
                        )
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            Text(stringResource(emptyMessage))
                        }
                    }
                }
                is HomeScreenState.Success -> {
                    LazyVerticalGrid(
                        modifier = contentModifier,
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
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
                                    selectionActive = selectedAccounts.isNotEmpty(),
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
    }

    val searchResults: @Composable ColumnScope.() -> Unit = {
        SearchResults(
            searchAccounts = searchAccounts,
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
    modifier: Modifier = Modifier
) {
    var isSortVisible by remember { mutableStateOf(false) }
    val sortLabel = stringResource(R.string.home_sort_title)
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(text = sortLabel) } },
        state = rememberTooltipState(),
        content = {
            IconButton(onClick = { isSortVisible = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_sort),
                    contentDescription = sortLabel,
                )
                DropdownMenuPopup(
                    expanded = isSortVisible,
                    onDismissRequest = { isSortVisible = false },
                ) {
                    DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                        SortSetting.entries.forEachIndexed { index, sortSetting ->
                            DropdownMenuItem(
                                selected = activeSortSetting == sortSetting,
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
                                selectedLeadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null
                                    )
                                },
                                shapes = MenuDefaults.itemShape(
                                    index = index,
                                    count = SortSetting.entries.size
                                ),
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun MoreAction(
    onMenuNavigate: (HomeMoreMenu) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMoreVisible by remember { mutableStateOf(false) }
    val moreLabel = stringResource(R.string.home_more_options)
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(text = moreLabel) } },
        state = rememberTooltipState(),
        content = {
            IconButton(onClick = { isMoreVisible = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = moreLabel,
                )
                DropdownMenuPopup(
                    expanded = isMoreVisible,
                    onDismissRequest = { isMoreVisible = false },
                ) {
                    val groupedActions = HomeMoreMenu.entries
                        .partition { it != HomeMoreMenu.Settings && it != HomeMoreMenu.About }
                        .toList()

                    groupedActions.forEachIndexed { groupIndex, actions ->
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(
                                index = groupIndex,
                                count = groupedActions.size
                            )
                        ) {
                            actions.forEachIndexed { actionIndex, action ->
                                DropdownMenuItem(
                                    onClick = {
                                        isMoreVisible = false
                                        onMenuNavigate(action)
                                    },
                                    text = { Text(stringResource(action.title)) },
                                    shape = MenuDefaults.itemShape(
                                        index = actionIndex,
                                        count = actions.size
                                    ).shape,
                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                            painter = painterResource(action.icon),
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
                    }
                }
            }
        },
    )
}

@Composable
private fun ColumnScope.SearchResults(
    searchAccounts: ImmutableList<DomainAccount>,
    searchTextFieldState: TextFieldState,
    onAccountEdit: (UUID) -> Unit,
    onAccountCounterIncrease: (UUID) -> Unit,
    onAccountCopyCode: (String, String, Boolean) -> Unit,
    selectedAccounts: SnapshotStateList<UUID>,
    accountRealtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
) {
    val query = searchTextFieldState.text.toString().trim()
    val filteredAccounts = remember(searchAccounts, query) {
        when {
            query.isEmpty() -> searchAccounts
            else -> searchAccounts
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
                        selectionActive = selectedAccounts.isNotEmpty(),
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
    canMoveToGroup: Boolean,
    onMoveToGroup: () -> Unit,
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
            if (canMoveToGroup) {
                val moveLabel = stringResource(R.string.home_move_title)
                TooltipBox(
                    modifier = Modifier,
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                    tooltip = { this.PlainTooltip { Text(text = moveLabel) } },
                    state = rememberTooltipState(),
                    content = {
                        IconButton(onClick = onMoveToGroup) {
                            Icon(
                                painter = painterResource(R.drawable.ic_label),
                                contentDescription = moveLabel,
                            )
                        }
                    },
                )
            }
            val deleteLabel = stringResource(R.string.home_selection_delete)
            TooltipBox(
                modifier = Modifier,
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { this.PlainTooltip { Text(text = deleteLabel) } },
                state = rememberTooltipState(),
                content = {
                        IconButton(onClick = onDeleteSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete_forever),
                                contentDescription = deleteLabel,
                            )
                        }
                    },
            )
            val exportLabel = stringResource(R.string.export_title)
            TooltipBox(
                modifier = Modifier,
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { this.PlainTooltip { Text(text = exportLabel) } },
                state = rememberTooltipState(),
                content = {
                    IconButton(onClick = onExportSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_export),
                            contentDescription = exportLabel,
                        )
                    }
                },
            )
        },
        scrollBehavior = scrollBehavior,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupFilterRow(
    groups: ImmutableList<DomainGroup>,
    activeGroup: GroupFilter,
    onActiveGroupChange: (GroupFilter) -> Unit,
    onAddGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "all", contentType = 0) {
            val selected = activeGroup is GroupFilter.All
            FilterChip(
                selected = selected,
                onClick = { onActiveGroupChange(GroupFilter.All) },
                label = { Text(stringResource(R.string.home_groups_all)) },
                leadingIcon = if (!selected) null else { ->
                    Icon(
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null
                    )
                }
            )
        }
        item(key = "ungrouped", contentType = 0) {
            val selected = activeGroup is GroupFilter.Ungrouped
            FilterChip(
                selected = selected,
                onClick = { onActiveGroupChange(GroupFilter.Ungrouped) },
                label = { Text(stringResource(R.string.home_groups_ungrouped)) },
                leadingIcon = if (!selected) null else { ->
                    Icon(
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null
                    )
                }
            )
        }
        item(key = "divider", contentType = 1) {
            VerticalDivider(modifier = Modifier.height(24.dp))
        }
        items(items = groups, key = { it.id }) { group ->
            val selected = activeGroup is GroupFilter.Specific && activeGroup.id == group.id
            val emoji = group.emoji
            FilterChip(
                selected = selected,
                onClick = { onActiveGroupChange(GroupFilter.Specific(group.id)) },
                label = { Text(group.name) },
                leadingIcon = when {
                    selected -> { ->
                        Icon(
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null
                        )
                    }
                    emoji != null -> { ->
                        Text(text = emoji, fontSize = 16.sp)
                    }
                    else -> null
                }
            )
        }
        item(key = "add") {
            val addGroupLabel = stringResource(R.string.groups_action_add_group)
            TooltipBox(
                modifier = Modifier,
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { this.PlainTooltip { Text(text = addGroupLabel) } },
                state = rememberTooltipState(),
                content = {
                    FilledIconButton(
                        modifier = Modifier.size(IconButtonDefaults.extraSmallContainerSize(widthOption = IconButtonDefaults.IconButtonWidthOption.Wide)),
                        onClick = onAddGroup,
                        shapes = IconButtonDefaults.shapes(shape = IconButtonDefaults.extraSmallSquareShape)
                    ) {
                        Icon(
                            modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize),
                            painter = painterResource(R.drawable.ic_new_label),
                            contentDescription = addGroupLabel,
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MoveToGroupSheet(
    groups: ImmutableList<DomainGroup>,
    onSelectGroup: (UUID?) -> Unit,
    onCreateGroup: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Expanded,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.home_move_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item(key = "create") {
                ListItem(
                    modifier = Modifier.fillParentMaxWidth(),
                    onClick = onCreateGroup,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_new_label),
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        leadingContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.home_move_action_create))
                }
            }
            item(key = "remove") {
                ListItem(
                    modifier = Modifier.fillParentMaxWidth(),
                    onClick = { onSelectGroup(null) },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_label_off),
                            contentDescription = null,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        leadingContentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.home_move_action_remove))
                }
            }
            item(key = "divider") {
                Text(
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp),
                    text = stringResource(R.string.home_move_subtitle_existing),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            itemsIndexed(items = groups, key = { _, group -> group.id }) { index, group ->
                ListItem(
                    modifier = Modifier.fillParentMaxWidth(),
                    onClick = { onSelectGroup(group.id) },
                    leadingContent = {
                        if (group.emoji != null) {
                            Text(group.emoji)
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_label),
                                contentDescription = null,
                            )
                        }
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = groups.size)
                ) {
                    Text(group.name)
                }
            }
        }
    }
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

@Composable
@PreviewAllConfigurations
private fun HomeScreen_Loading_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onAddAccountNavigate = {},
                onMoreMenuNavigate = {},
                onAccountSelect = {},
                onCancelAccountSelection = {},
                onDeleteSelectedAccounts = {},
                onExportSelectedAccounts = {},
                onAccountEdit = {},
                onAccountCounterIncrease = {},
                onAccountCopyCode = { _, _, _ -> },
                state = HomeScreenState.Loading,
                accountRealtimeData = remember { mutableStateMapOf<UUID, DomainOtpRealtimeData>() },
                selectedAccounts = remember { mutableStateListOf<UUID>() },
                activeSortSetting = SortSetting.entries.first(),
                onActiveSortChange = {},
                groups = persistentListOf(),
                activeGroup = GroupFilter.All,
                onActiveGroupChange = {},
                onCreateGroupClick = {},
                onGroupSelectedClick = {},
                searchAccounts = persistentListOf(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun HomeScreen_Empty_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onAddAccountNavigate = {},
                onMoreMenuNavigate = {},
                onAccountSelect = {},
                onCancelAccountSelection = {},
                onDeleteSelectedAccounts = {},
                onExportSelectedAccounts = {},
                onAccountEdit = {},
                onAccountCounterIncrease = {},
                onAccountCopyCode = { _, _, _ -> },
                state = HomeScreenState.Empty,
                accountRealtimeData = remember { mutableStateMapOf<UUID, DomainOtpRealtimeData>() },
                selectedAccounts = remember { mutableStateListOf<UUID>() },
                activeSortSetting = SortSetting.entries.first(),
                onActiveSortChange = {},
                groups = persistentListOf(),
                activeGroup = GroupFilter.All,
                onActiveGroupChange = {},
                onCreateGroupClick = {},
                onGroupSelectedClick = {},
                searchAccounts = persistentListOf(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun HomeScreen_Success_Preview() {
    val totp = DomainAccount.Totp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "GitHub",
        issuer = "github.com",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
        period = 30
    )
    val hotp = DomainAccount.Hotp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "Amazon",
        issuer = "amazon.com",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L
    )
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onAddAccountNavigate = {},
                onMoreMenuNavigate = {},
                onAccountSelect = {},
                onCancelAccountSelection = {},
                onDeleteSelectedAccounts = {},
                onExportSelectedAccounts = {},
                onAccountEdit = {},
                onAccountCounterIncrease = {},
                onAccountCopyCode = { _, _, _ -> },
                state = HomeScreenState.Success(persistentListOf(totp, hotp)),
                accountRealtimeData = remember {
                    mutableStateMapOf(
                        totp.id to DomainOtpRealtimeData.Totp(code = "123456", progress = 0.6f, countdown = 18),
                        hotp.id to DomainOtpRealtimeData.Hotp(code = "654321", count = 3)
                    )
                },
                selectedAccounts = remember { mutableStateListOf<UUID>() },
                activeSortSetting = SortSetting.entries.first(),
                onActiveSortChange = {},
                groups = persistentListOf(),
                activeGroup = GroupFilter.All,
                onActiveGroupChange = {},
                onCreateGroupClick = {},
                onGroupSelectedClick = {},
                searchAccounts = persistentListOf(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun HomeScreen_Selection_Preview() {
    val totp = DomainAccount.Totp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "GitHub",
        issuer = "github.com",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
        period = 30
    )
    val hotp = DomainAccount.Hotp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "Amazon",
        issuer = "amazon.com",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L
    )
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onAddAccountNavigate = {},
                onMoreMenuNavigate = {},
                onAccountSelect = {},
                onCancelAccountSelection = {},
                onDeleteSelectedAccounts = {},
                onExportSelectedAccounts = {},
                onAccountEdit = {},
                onAccountCounterIncrease = {},
                onAccountCopyCode = { _, _, _ -> },
                state = HomeScreenState.Success(persistentListOf(totp, hotp)),
                accountRealtimeData = remember {
                    mutableStateMapOf(
                        totp.id to DomainOtpRealtimeData.Totp(code = "123456", progress = 0.6f, countdown = 18),
                        hotp.id to DomainOtpRealtimeData.Hotp(code = "654321", count = 3)
                    )
                },
                selectedAccounts = remember { mutableStateListOf(totp.id) },
                activeSortSetting = SortSetting.entries.first(),
                onActiveSortChange = {},
                groups = persistentListOf(),
                activeGroup = GroupFilter.All,
                onActiveGroupChange = {},
                onCreateGroupClick = {},
                onGroupSelectedClick = {},
                searchAccounts = persistentListOf(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun HomeScreen_Groups_Preview() {
    val totp = DomainAccount.Totp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "GitHub",
        issuer = "github.com",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
        period = 30
    )
    val hotp = DomainAccount.Hotp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "Amazon",
        issuer = "amazon.com",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L
    )
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onAddAccountNavigate = {},
                onMoreMenuNavigate = {},
                onAccountSelect = {},
                onCancelAccountSelection = {},
                onDeleteSelectedAccounts = {},
                onExportSelectedAccounts = {},
                onAccountEdit = {},
                onAccountCounterIncrease = {},
                onAccountCopyCode = { _, _, _ -> },
                state = HomeScreenState.Success(persistentListOf(totp, hotp)),
                accountRealtimeData = remember {
                    mutableStateMapOf(
                        totp.id to DomainOtpRealtimeData.Totp(code = "123456", progress = 0.6f, countdown = 18),
                        hotp.id to DomainOtpRealtimeData.Hotp(code = "654321", count = 3)
                    )
                },
                selectedAccounts = remember { mutableStateListOf<UUID>() },
                activeSortSetting = SortSetting.entries.first(),
                onActiveSortChange = {},
                groups = persistentListOf(
                    DomainGroup(
                        id = UUID.fromString("00000000-0000-0000-0000-0000000000a1"),
                        name = "Work",
                        emoji = "💼",
                        sortIndex = 0
                    ),
                    DomainGroup(
                        id = UUID.fromString("00000000-0000-0000-0000-0000000000a2"),
                        name = "Personal",
                        emoji = null,
                        sortIndex = 1
                    )
                ),
                activeGroup = GroupFilter.All,
                onActiveGroupChange = {},
                onCreateGroupClick = {},
                onGroupSelectedClick = {},
                searchAccounts = persistentListOf(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun HomeScreen_Error_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeScreen(
                onAddAccountNavigate = {},
                onMoreMenuNavigate = {},
                onAccountSelect = {},
                onCancelAccountSelection = {},
                onDeleteSelectedAccounts = {},
                onExportSelectedAccounts = {},
                onAccountEdit = {},
                onAccountCounterIncrease = {},
                onAccountCopyCode = { _, _, _ -> },
                state = HomeScreenState.Error("Something went wrong"),
                accountRealtimeData = remember { mutableStateMapOf<UUID, DomainOtpRealtimeData>() },
                selectedAccounts = remember { mutableStateListOf<UUID>() },
                activeSortSetting = SortSetting.entries.first(),
                onActiveSortChange = {},
                groups = persistentListOf(),
                activeGroup = GroupFilter.All,
                onActiveGroupChange = {},
                onCreateGroupClick = {},
                onGroupSelectedClick = {},
                searchAccounts = persistentListOf(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}