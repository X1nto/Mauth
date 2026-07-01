package com.xinto.mauth.ui.screen.home.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.ui.screen.home.HomeAddAccountMenu
import com.xinto.mauth.ui.screen.home.HomeMoreMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    isSelectionActive: Boolean = false,
    selectedCount: Int = 0,
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit,
    onAddAccountNavigate: (HomeAddAccountMenu) -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExportSelected: () -> Unit,
    onMenuNavigate: (HomeMoreMenu) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
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
                        selectedCount = selectedCount,
                        onCancelSelection = onCancelSelection,
                        onDeleteSelected = onDeleteSelected,
                        onExportSelected = onExportSelected,
                        scrollBehavior = scrollBehavior,
                    )
                } else {
                    MainTopBar(
                        activeSortSetting = activeSortSetting,
                        onActiveSortChange = onActiveSortChange,
                        onMenuNavigate = onMenuNavigate,
                        scrollBehavior = scrollBehavior,
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
        },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit,
    onMenuNavigate: (HomeMoreMenu) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
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
                    SortSetting.entries.forEach {
                        DropdownMenuItem(
                            onClick = {
                                isSortVisible = false
                                onActiveSortChange(it)
                            },
                            text = {
                                val resource = remember(it) {
                                    when (it) {
                                        SortSetting.DateAsc -> R.string.home_sort_date_ascending
                                        SortSetting.DateDesc -> R.string.home_sort_date_descending
                                        SortSetting.LabelAsc -> R.string.home_sort_label_ascending
                                        SortSetting.LabelDesc -> R.string.home_sort_label_descending
                                        SortSetting.IssuerAsc -> R.string.home_sort_issuer_ascending
                                        SortSetting.IssuerDesc -> R.string.home_sort_issuer_descending
                                    }
                                }
                                Text(stringResource(resource))
                            },
                            trailingIcon = {
                                if (activeSortSetting == it) {
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
        },
        scrollBehavior = scrollBehavior,
    )
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
        title = {
            Text(pluralStringResource(R.plurals.home_selection_count, selectedCount, selectedCount))
        },
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
