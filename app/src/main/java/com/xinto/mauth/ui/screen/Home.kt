package com.xinto.mauth.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.contracts.PickVisualMediaPersistent
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.ui.component.FullWidthButton
import com.xinto.mauth.ui.component.MaterialBottomSheetDialog
import com.xinto.mauth.ui.component.TwoPaneCard
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.navigation.MauthDestination
import com.xinto.mauth.ui.navigation.MauthNavigator
import com.xinto.mauth.ui.viewmodel.HomeViewModel
import org.koin.androidx.compose.getViewModel

sealed interface HomeState {
    object Loading : HomeState
    object Loaded : HomeState
    object Failed : HomeState
}

sealed interface HomeBottomBarState {
    object Normal : HomeBottomBarState
    object Selection : HomeBottomBarState
}

@Composable
fun HomeScreen(
    navigator: MauthNavigator,
    viewModel: HomeViewModel = getViewModel(),
) {
    var showAddAccount by remember { mutableStateOf(false) }
    var showDeleteAccounts by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                }
            )
        },
        bottomBar = {
            BottomBar(
                state = viewModel.bottomBarState,
                onSettingsClick = {
                    navigator.push(MauthDestination.Settings)
                },
                onDeleteAccountsClick = {
                    showDeleteAccounts = true
                },
                onAddAccount = {
                    showAddAccount = true
                },
                onClearSelection = {
                    viewModel.clearSelection()
                }
            )
        }
    ) { paddingValues ->
        if (viewModel.accounts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(viewModel.accounts) { account ->
                    var visible by remember { mutableStateOf(false) }
                    val code = viewModel.codes[account.id]
                    AccountCard(
                        account = account,
                        code = code,
                        onCopyClick = {
                            viewModel.copyCodeToClipboard(account.label, code)
                        },
                        onEditClick = {
                            navigator.push(MauthDestination.EditAccount(account.id))
                        },
                        onLongClick = {
                            viewModel.selectUnselectAccount(account.id)
                        },
                        onClick = {
                            //don't require long clicks if one of the accounts is already selected
                            if (viewModel.selectedAccounts.isNotEmpty()) {
                                viewModel.selectUnselectAccount(account.id)
                            }
                        },
                        selected = viewModel.selectedAccounts.contains(account.id),
                        expanded = viewModel.selectedAccounts.isEmpty(),
                        onVisibleToggle = {
                            visible = !visible
                        },
                        indicator = {
                            when (account) {
                                is DomainAccount.Totp -> {
                                    Box(
                                        modifier = Modifier.size(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val timerProgress = viewModel.timerProgresses[account.id]
                                        val timerValue = viewModel.timerValues[account.id]
                                        if (timerProgress != null) {
                                            val animatedTimerProgress by animateFloatAsState(
                                                targetValue = timerProgress,
                                                animationSpec = tween(durationMillis = 500)
                                            )
                                            CircularProgressIndicator(progress = animatedTimerProgress)
                                        }
                                        if (timerValue != null) {
                                            Text(timerValue.toString())
                                        }
                                    }
                                }
                                is DomainAccount.Hotp -> {
                                    FilledIconButton(onClick = {
                                        viewModel.incrementAccountCounter(account.id)
                                    }) {
                                        Text(account.counter.toString())
                                    }
                                }
                            }
                        },
                        visible = visible,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(72.dp),
                    imageVector = Icons.Rounded.Dashboard,
                    contentDescription = null
                )
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    Text(stringResource(R.string.home_dashboard_empty))
                }
            }
        }
    }

    if (showAddAccount) {
        val photoPickerLauncher =
            rememberLauncherForActivityResult(PickVisualMediaPersistent()) { uri ->
                viewModel.parseImageUri(uri)?.let {
                    navigator.push(MauthDestination.AddAccount(it))
                }
            }
        AddAccountDialog(
            onDismiss = {
                showAddAccount = false
            },
            onQrScanClick = {
                showAddAccount = false
                navigator.push(MauthDestination.QrScanner)
            },
            onImageChooseClick = {
                showAddAccount = false
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onManualEnterClick = {
                showAddAccount = false
                navigator.push(MauthDestination.AddAccount(DomainAccountInfo.DEFAULT))
            }
        )
    }

    if (showDeleteAccounts) {
        DeleteAccountsDialog(
            onDismiss = {
                showDeleteAccounts = false
            },
            onConfirm = {
                showDeleteAccounts = false
                viewModel.deleteSelected()
            }
        )
    }
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onQrScanClick: () -> Unit,
    onImageChooseClick: () -> Unit,
    onManualEnterClick: () -> Unit,
) {
    MaterialBottomSheetDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.home_addaccount_title))
        },
        subtitle = {
            Text(stringResource(R.string.home_addaccount_subtitle))
        },
    ) {
        Column(
            modifier = Modifier.clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            FullWidthButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onQrScanClick,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.QrCodeScanner,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.home_addaccount_data_scanqr))
                },
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            FullWidthButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onImageChooseClick,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.home_addaccount_data_imageqr))
                },
                color = MaterialTheme.colorScheme.tertiaryContainer,
            )
            FullWidthButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onManualEnterClick,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Password,
                        contentDescription = null
                    )
                },
                text = {
                    Text(stringResource(R.string.home_addaccount_data_manual))
                },
                color = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

@Composable
private fun DeleteAccountsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.DeleteForever,
                contentDescription = null
            )
        },
        title = {
            Text(stringResource(R.string.home_delete_title))
        },
        text = {
            Text(stringResource(R.string.home_delete_subtitle))
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text(stringResource(R.string.home_delete_button_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_delete_button_cancel))
            }
        }
    )
}

@Composable
private fun BottomBar(
    state: HomeBottomBarState,
    onSettingsClick: () -> Unit,
    onDeleteAccountsClick: () -> Unit,
    onAddAccount: () -> Unit,
    onClearSelection: () -> Unit
) {
    BottomAppBar(
        floatingActionButton = {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Start) + fadeIn() with
                        slideOutOfContainer(AnimatedContentScope.SlideDirection.Start) + fadeOut()
                }
            ) { bottomBarState ->
                when (bottomBarState) {
                    HomeBottomBarState.Normal -> {
                        AddAccountFab(onClick = onAddAccount)
                    }
                    HomeBottomBarState.Selection -> {
                        ReturnFab(onClick = onClearSelection)
                    }
                }
            }
        },
        actions = {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    if (HomeBottomBarState.Normal isTransitioningTo HomeBottomBarState.Selection) {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Up) + fadeIn() with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
                    } else {
                        slideIntoContainer(AnimatedContentScope.SlideDirection.Down) + fadeIn() with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Down) + fadeOut()
                    }
                }
            ) { bottomBarState ->
                when (bottomBarState) {
                    HomeBottomBarState.Normal -> {
                        BottombarNormalActions(
                            onSettingsClick = onSettingsClick,
                            onSearchClick = { /*TODO*/ },
                            onSortClick = { /*TODO*/ },
                        )
                    }
                    HomeBottomBarState.Selection -> {
                        BottombarSelectionActions(onDeleteAccountsClick = onDeleteAccountsClick)
                    }
                }
            }
        },
    )
}

@Composable
fun BottombarNormalActions(
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
) {
    Row {
        var moreMenuExpanded by remember { mutableStateOf(false) }
        IconButton(onClick = { moreMenuExpanded = true }) {
            DropdownMenu(
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        moreMenuExpanded = false
                        onSettingsClick()
                    },
                    text = { Text(stringResource(R.string.home_more_settings)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null
                        )
                    }
                )
            }
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = null
            )
        }
        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null
            )
        }
        IconButton(onClick = onSortClick) {
            Icon(
                imageVector = Icons.Rounded.Sort,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun BottombarSelectionActions(
    onDeleteAccountsClick: () -> Unit
) {
    IconButton(
        onClick = onDeleteAccountsClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = null
        )
    }
}

@Composable
private fun ReturnFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Rounded.Undo,
            contentDescription = null
        )
    }
}

@Composable
private fun AddAccountFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null
        )
    }
}

@Composable
fun AccountCard(
    account: DomainAccount,
    code: String?,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    selected: Boolean,
    expanded: Boolean,
    onVisibleToggle: (Boolean) -> Unit,
    visible: Boolean,
    indicator: @Composable () -> Unit,
) {
    TwoPaneCard(
        selected = selected,
        expanded = expanded,
        onClick = onClick,
        onLongClick = onLongClick,
        topContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccountIcon(
                    icon = account.icon,
                    shortLabel = account.shortLabel
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (account.issuer != "") {
                        Text(
                            text = account.issuer,
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalContentColor.current.copy(alpha = 0.7f)
                        )
                    }
                    Text(account.label, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(Modifier.weight(1f))
                if (selected) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        bottomContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                indicator()
                AccountCode(code, visible, account.digits)
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconToggleButton(
                        checked = visible,
                        onCheckedChange = onVisibleToggle
                    ) {
                        Icon(
                            imageVector = if (visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = null
                        )
                    }
                    FilledTonalIconButton(onClick = onCopyClick) {
                        Icon(
                            imageVector = Icons.Rounded.CopyAll,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun AccountCode(
    code: String?,
    visible: Boolean,
    digits: Int
) {
    AnimatedContent(
        targetState = code,
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Up,
                animationSpec = tween(500)
            ) + fadeIn() with
                slideOutOfContainer(
                    towards = AnimatedContentScope.SlideDirection.Up,
                    animationSpec = tween(500)
                ) + fadeOut()
        }
    ) { animatedCode ->
        if (animatedCode != null && visible) {
            Text(animatedCode, style = MaterialTheme.typography.titleLarge)
        } else {
            Text("\u2022".repeat(digits), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun AccountIcon(
    icon: Uri?,
    shortLabel: String
) {
    if (icon != null) {
        UriImage(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.medium),
            uri = icon
        )
    } else {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(shortLabel, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}