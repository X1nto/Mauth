package com.xinto.mauth.ui.screen.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.component.lazygroup.GroupedItemType
import com.xinto.mauth.ui.component.lazygroup.GroupedListItem
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GroupsScreen(
    onBack: () -> Unit,
    onAddAccount: (groupId: UUID?) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: GroupsViewModel = koinViewModel()
    val model by viewModel.uiModel.collectAsStateWithLifecycle()

    GroupsScreen(
        state = model,
        onBack = onBack,
        onAddAccount = onAddAccount,
        onMoveAccountToGroup = viewModel::moveAccountToGroup,
        onReorderGroups = viewModel::reorderGroups,
        onMoveGroupUp = viewModel::moveUp,
        onMoveGroupDown = viewModel::moveDown,
        onCreateGroup = viewModel::createGroup,
        onUpdateGroup = viewModel::updateGroup,
        onDeleteGroup = viewModel::deleteGroup,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    state: GroupsState,
    onBack: () -> Unit,
    onAddAccount: (groupId: UUID?) -> Unit,
    onMoveAccountToGroup: (accountId: UUID, groupId: UUID?) -> Unit,
    onReorderGroups: (orderedIds: List<UUID>) -> Unit,
    onMoveGroupUp: (UUID) -> Unit,
    onMoveGroupDown: (UUID) -> Unit,
    onCreateGroup: (name: String, emoji: String?) -> Unit,
    onUpdateGroup: (id: UUID, name: String, emoji: String?) -> Unit,
    onDeleteGroup: (id: UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<DomainGroup?>(null) }
    var deleteTarget by remember { mutableStateOf<DomainGroup?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.groups_title)) },
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                text = { Text(stringResource(R.string.groups_action_add_group)) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null
                    )
                }
            )
        }
    ) { innerPadding ->
        if (state.isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.groups_state_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            GroupsTree(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                model = state,
                onAddAccount = onAddAccount,
                onMoveAccountToGroup = onMoveAccountToGroup,
                onReorderGroups = onReorderGroups,
                onMoveGroupUp = onMoveGroupUp,
                onMoveGroupDown = onMoveGroupDown,
                onRename = { renameTarget = it },
                onDelete = { deleteTarget = it }
            )
        }
    }

    if (showCreateDialog) {
        CreateGroupDialog(
            isNameTaken = { candidate -> state.groupSections.any { it.group.name.equals(candidate, ignoreCase = true) } },
            onConfirm = { name, emoji ->
                onCreateGroup(name, emoji)
                showCreateDialog = false
            },
            onDismissRequest = { showCreateDialog = false }
        )
    }

    if (renameTarget != null) {
        EditGroupDialog(
            initialName = renameTarget!!.name,
            initialEmoji = renameTarget!!.emoji,
            isNameTaken = { candidate ->
                state.groupSections.any {
                    it.group.id != renameTarget!!.id && it.group.name.equals(candidate, ignoreCase = true)
                }
            },
            onConfirm = { name, emoji ->
                onUpdateGroup(renameTarget!!.id, name, emoji)
                renameTarget = null
            },
            onDismissRequest = { renameTarget = null }
        )
    }
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever),
                    contentDescription = null
                )
            },
            title = { Text(stringResource(R.string.groups_delete_title)) },
            text = { Text(stringResource(R.string.groups_delete_subtitle)) },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        onDeleteGroup(deleteTarget!!.id)
                        deleteTarget = null
                    }
                ) {
                    Text(stringResource(R.string.groups_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.groups_dialog_action_cancel))
                }
            }
        )
    }
}

// FIXME garbage code below
@Composable
private fun GroupsTree(
    model: GroupsState,
    modifier: Modifier = Modifier,
    onAddAccount: (groupId: UUID?) -> Unit,
    onMoveAccountToGroup: (accountId: UUID, groupId: UUID?) -> Unit,
    onReorderGroups: (orderedIds: List<UUID>) -> Unit,
    onMoveGroupUp: (UUID) -> Unit,
    onMoveGroupDown: (UUID) -> Unit,
    onRename: (DomainGroup) -> Unit,
    onDelete: (DomainGroup) -> Unit
) {
    val lazyListState = rememberLazyListState()

    val keyTargets = remember(model) { buildKeyTargets(buildRows(model)) }
    val drag = rememberAccountDragHandler(
        lazyListState = lazyListState,
        keyTargets = keyTargets,
        onMove = onMoveAccountToGroup
    )

    var draggingGroup by remember { mutableStateOf(false) }
    val dragging = drag.isDragging || draggingGroup

    var list by remember { mutableStateOf(buildRows(model)) }
    LaunchedEffect(model, dragging) { if (!dragging) list = buildRows(model) }

    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromRow = list.firstOrNull { it.key == from.key }
        if (fromRow is GroupRow.GroupHeader) {
            val toRow = list.firstOrNull { it.key == to.key }
            if (toRow != null && toRow !is GroupRow.UngroupedHeader) {
                val fromIndex = list.indexOfFirst { it.key == from.key }
                val toIndex = list.indexOfFirst { it.key == to.key }
                if (fromIndex >= 0 && toIndex >= 0) {
                    list = list.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
                }
            }
        }
    }

    LaunchedEffect(drag.isDragging) {
        if (!drag.isDragging) return@LaunchedEffect
        while (true) {
            val info = lazyListState.layoutInfo
            val edge = 140f
            val delta = when {
                drag.pointerY < info.viewportStartOffset + edge -> -14f
                drag.pointerY > info.viewportEndOffset - edge -> 14f
                else -> 0f
            }
            if (delta != 0f) {
                lazyListState.scrollBy(delta)
                drag.refreshHoveredTarget()
            }
            delay(16.milliseconds)
        }
    }

    val counts = remember(model) { model.groupSections.associate { it.group.id to it.accounts.size }.toImmutableMap() }
    val allGroups = remember(model) { model.groupSections.map { it.group }.toImmutableList() }
    val canReorderGroups = model.groupSections.size >= 2
    val groupOrder = currentGroupOrder(list)

    // LazyColumn and AccountDragPreview need to share a common parent for the preview to
    // properly reference coordinates
    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(
                items = list,
                key = { _, row -> row.key },
                contentType = { _, row -> row::class }
            ) { index, row ->
                when (row) {
                    is GroupRow.GroupHeader -> {
                        ReorderableItem(reorderState, key = row.key) { isDragging ->
                            val groupIndex = groupOrder.indexOf(row.group.id)
                            GroupHeaderRow(
                                group = row.group,
                                count = counts[row.group.id] ?: 0,
                                isDragging = isDragging,
                                isDropTarget = drag.isOver(row.group.id),
                                showDragHandle = canReorderGroups,
                                canMoveUp = groupIndex > 0,
                                canMoveDown = groupIndex in 0 until groupOrder.lastIndex,
                                onReorderStart = {
                                    draggingGroup = true
                                    list = list.filter { it is GroupRow.GroupHeader || it is GroupRow.UngroupedHeader }
                                },
                                onReorderStop = {
                                    draggingGroup = false
                                    onReorderGroups(currentGroupOrder(list))
                                },
                                onMoveUp = { onMoveGroupUp(row.group.id) },
                                onMoveDown = { onMoveGroupDown(row.group.id) },
                                onRename = { onRename(row.group) },
                                onDelete = { onDelete(row.group) }
                            )
                        }
                    }
                    is GroupRow.AccountItem -> {
                        AccountRowItem(
                            account = row.account,
                            groupId = row.groupId,
                            type = accountType(list, index),
                            dragHandler = drag,
                            groups = allGroups,
                            onMoveTo = { target -> onMoveAccountToGroup(row.account.id, target) }
                        )
                    }
                    is GroupRow.AddAccountRow -> {
                        AddAccountRow(
                            isDropTarget = drag.isOver(row.groupId),
                            isDragging = drag.isDragging,
                            onAddAccount = { onAddAccount(row.groupId) }
                        )
                    }
                    GroupRow.UngroupedHeader -> {
                        UngroupedHeaderRow(
                            count = model.ungroupedCount,
                            isDropTarget = drag.isOver(null)
                        )
                    }
                }
            }
        }

        if (drag.draggedAccount != null) {
            AccountDragPreview(
                account = drag.draggedAccount!!,
                modifier = Modifier
                    .offset { IntOffset(0, (drag.pointerY - drag.grabOffsetY).roundToInt()) }
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.GroupHeaderRow(
    group: DomainGroup,
    count: Int,
    isDragging: Boolean,
    isDropTarget: Boolean,
    showDragHandle: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onReorderStart: () -> Unit,
    onReorderStop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val background = when {
        isDragging -> MaterialTheme.colorScheme.surfaceContainerHigh
        isDropTarget -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val moveUpLabel = stringResource(R.string.groups_action_move_up)
        val moveDownLabel = stringResource(R.string.groups_action_move_down)
        Icon(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .draggableHandle(
                    enabled = showDragHandle,
                    onDragStarted = { onReorderStart() },
                    onDragStopped = onReorderStop
                )
                .semantics {
                    customActions = buildList {
                        if (canMoveUp) {
                            add(CustomAccessibilityAction(moveUpLabel) {
                                onMoveUp()
                                true
                            })
                        }
                        if (canMoveDown) {
                            add(CustomAccessibilityAction(moveDownLabel) {
                                onMoveDown()
                                true
                            })
                        }
                    }
                },
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = stringResource(R.string.groups_action_reorder),
            tint = LocalContentColor.current.copy(alpha = if (showDragHandle) 1f else 0.5f)
        )
        if (group.emoji != null) {
            Text(text = group.emoji, fontSize = 20.sp)
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_label),
                contentDescription = null
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = pluralStringResource(R.plurals.groups_account_count, count, count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vert),
                    contentDescription = null
                )
            }
            DropdownMenuPopup(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                    DropdownMenuItem(
                        onClick = {
                            menuOpen = false
                            onRename()
                        },
                        text = { Text(stringResource(R.string.groups_action_rename)) },
                        shape = MenuDefaults.itemShape(index = 0, count = 2).shape,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                        text = { Text(stringResource(R.string.groups_action_delete)) },
                        shape = MenuDefaults.itemShape(index = 1, count = 2).shape,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UngroupedHeaderRow(count: Int, isDropTarget: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (isDropTarget) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 12.dp),
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = stringResource(R.string.groups_action_reorder),
            tint = LocalContentColor.current.copy(alpha = 0.5f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_label_off),
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_groups_ungrouped),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = pluralStringResource(R.plurals.groups_account_count, count, count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountRowItem(
    account: DomainAccount,
    groupId: UUID?,
    type: GroupedItemType,
    dragHandler: AccountDragHandler,
    groups: ImmutableList<DomainGroup>,
    onMoveTo: (UUID?) -> Unit
) {
    val moveToTemplate = stringResource(R.string.groups_action_group)
    val moveToUngroupedLabel = stringResource(R.string.groups_action_ungrouped)

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .alpha(if (dragHandler.isDragging(account.id)) 0.3f else 1f)
            .semantics {
                customActions = buildList {
                    groups.forEach { group ->
                        if (group.id != groupId) {
                            add(CustomAccessibilityAction(moveToTemplate.format(group.name)) {
                                onMoveTo(group.id)
                                true
                            })
                        }
                    }
                    if (groupId != null) {
                        add(CustomAccessibilityAction(moveToUngroupedLabel) {
                            onMoveTo(null)
                            true
                        })
                    }
                }
            }
    ) {
        val isHighlighted by remember {
            derivedStateOf {
                dragHandler.isOver(groupId)
            }
        }
        GroupedListItem(
            type = type,
            colors = ListItemDefaults.colors(
                containerColor = if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
            ),
            leadingContent = {
                AccountAvatar(
                    account = account,
                    color = if (isHighlighted) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.secondaryContainer
                )
            },
            headlineContent = {
                Text(
                    text = account.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = if (account.issuer.isEmpty()) null else { ->
                Text(
                    text = account.issuer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                Icon(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .then(dragHandler.handleFor(account, groupId)),
                    painter = painterResource(R.drawable.ic_drag_handle),
                    contentDescription = stringResource(R.string.groups_action_move)
                )
            }
        )
    }
}

@Composable
private fun AccountDragPreview(
    account: DomainAccount,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccountAvatar(account)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (account.issuer.isNotEmpty()) {
                    Text(
                        text = account.issuer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_drag_handle),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun AccountAvatar(
    account: DomainAccount,
    color: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            val icon = account.icon
            if (icon != null) {
                UriImage(uri = icon, modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    text = account.shortLabel,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun AddAccountRow(
    isDropTarget: Boolean,
    isDragging: Boolean,
    onAddAccount: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isDropTarget) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
            .clickable(enabled = !isDragging, onClick = onAddAccount)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isDragging) {
            Text(
                text = stringResource(R.string.groups_drop_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.groups_action_add_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun GroupsScreen_Empty_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GroupsScreen(
                state = GroupsState(persistentListOf()),
                onBack = {},
                onAddAccount = {},
                onMoveAccountToGroup = { _, _ -> },
                onReorderGroups = {},
                onMoveGroupUp = {},
                onMoveGroupDown = {},
                onCreateGroup = { _, _ -> },
                onUpdateGroup = { _, _, _ -> },
                onDeleteGroup = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun GroupsScreen_Populated_Preview() {
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
    val group = DomainGroup(
        id = UUID.fromString("00000000-0000-0000-0000-0000000000a1"),
        name = "Work",
        emoji = "💼",
        sortIndex = 0
    )
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GroupsScreen(
                state = GroupsState(
                    persistentListOf(
                        GroupSection.Grouped(group, persistentListOf(totp)),
                        GroupSection.Ungrouped(persistentListOf(hotp))
                    )
                ),
                onBack = {},
                onAddAccount = {},
                onMoveAccountToGroup = { _, _ -> },
                onReorderGroups = {},
                onMoveGroupUp = {},
                onMoveGroupDown = {},
                onCreateGroup = { _, _ -> },
                onUpdateGroup = { _, _, _ -> },
                onDeleteGroup = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
