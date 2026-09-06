@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.model.BackupFormat
import com.xinto.mauth.domain.account.model.DomainExportAccount
import com.xinto.mauth.ui.component.UriImage
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.theme.MauthTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun ExportScreen(
    accounts: List<UUID>,
    onBackNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ExportViewModel = koinViewModel {
        parametersOf(accounts)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var batchExportFormat by retain { mutableStateOf<BackupFormat?>(null) }
    if (batchExportFormat != null && state is ExportScreenState.Success) {
        val accounts = (state as ExportScreenState.Success).accounts.map { it.id }
        FileExportDialog(
            onDismissRequest = { batchExportFormat = null },
            format = batchExportFormat!!,
            accounts = accounts
        )
    }

    var exportAccount by retain { mutableStateOf<UUID?>(null) }
    if (exportAccount != null) {
        AccountExportDialog(
            onDismissRequest = { exportAccount = null },
            account = exportAccount!!
        )
    }

    var showGoogleAuthenticatorQrDialog by retain { mutableStateOf(false) }
    if (showGoogleAuthenticatorQrDialog && state is ExportScreenState.Success) {
        val accounts = (state as ExportScreenState.Success).accounts.map { it.id }
        GoogleAuthenticatorExportDialog(
            onDismissRequest = { showGoogleAuthenticatorQrDialog = false },
            accounts = accounts
        )
    }

    ExportScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        onBackNavigate = onBackNavigate,
        onGoogleAuthQr = { showGoogleAuthenticatorQrDialog = true },
        onMauthExport = { batchExportFormat = BackupFormat.MauthJson },
        onAegisExport = { batchExportFormat = BackupFormat.AegisVault },
        onFreeOtpPlusExport = { batchExportFormat = BackupFormat.FreeOtpPlusJson },
        onUriListExport = { batchExportFormat = BackupFormat.UriList },
        onAccountExport = { exportAccount = it }
    )
}

@Composable
fun ExportScreen(
    state: ExportScreenState,
    onBackNavigate: () -> Unit,
    onGoogleAuthQr: () -> Unit,
    onMauthExport: () -> Unit,
    onAegisExport: () -> Unit,
    onFreeOtpPlusExport: () -> Unit,
    onUriListExport: () -> Unit,
    onAccountExport: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
                subtitle = {
                    if (state is ExportScreenState.Success && !state.isAll) {
                        Text(pluralStringResource(R.plurals.export_subtitle, state.accounts.size))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackNavigate) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (state) {
            is ExportScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is ExportScreenState.Empty -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.export_state_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is ExportScreenState.Error -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error),
                        contentDescription = null
                    )
                }
            }
            is ExportScreenState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .widthIn(max = 600.dp)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item(key = "batch-header", contentType = "header") {
                        CategoryHeader(stringResource(R.string.export_category_batch))
                    }

                    item(key = "batch-googleauth", contentType = "batch-method") {
                        ListItem(
                            onClick = onGoogleAuthQr,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_qr_code_2),
                                    contentDescription = null
                                )
                            },
                            supportingContent = { Text(stringResource(id = R.string.export_method_googleauth_description)) },
                            shapes = ListItemDefaults.segmentedShapes(index = 0, count = 5),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(id = R.string.export_method_googleauth))
                        }
                    }

                    item(key = "batch-mauth", contentType = "batch-method") {
                        ListItem(
                            onClick = onMauthExport,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mauth),
                                    contentDescription = null
                                )
                            },
                            shapes = ListItemDefaults.segmentedShapes(index = 1, count = 5),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(id = R.string.export_method_mauth))
                        }
                    }

                    item(key = "batch-aegis", contentType = "batch-method") {
                        ListItem(
                            onClick = onAegisExport,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_aegis),
                                    contentDescription = null
                                )
                            },
                            shapes = ListItemDefaults.segmentedShapes(index = 2, count = 5),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(id = R.string.export_method_aegis))
                        }
                    }

                    item(key = "batch-freeotpplus", contentType = "batch-method") {
                        ListItem(
                            onClick = onFreeOtpPlusExport,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_freeotpplus),
                                    contentDescription = null
                                )
                            },
                            shapes = ListItemDefaults.segmentedShapes(index = 3, count = 5),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(id = R.string.export_method_freeotpplus))
                        }
                    }

                    item(key = "batch-urilist", contentType = "batch-method") {
                        ListItem(
                            onClick = onUriListExport,
                            modifier = Modifier,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_link),
                                    contentDescription = null
                                )
                            },
                            supportingContent = { Text(stringResource(id = R.string.export_method_urilist_description)) },
                            shapes = ListItemDefaults.segmentedShapes(index = 4, count = 5),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(id = R.string.export_method_urilist))
                        }
                    }

                    item(key = "individual-header", contentType = "header") {
                        CategoryHeader(
                            modifier = Modifier.padding(top = 14.dp),
                            text = stringResource(R.string.export_category_individual)
                        )
                    }

                    itemsIndexed(
                        items = state.accounts,
                        key = { _, account -> account.id },
                        contentType = { _, _ -> "individual-account" }
                    ) { index, account ->
                        ListItem(
                            verticalAlignment = Alignment.CenterVertically,
                            onClick = { onAccountExport(account.id) },
                            leadingContent = { AccountBadge(account) },
                            overlineContent = if (account.issuer.isBlank()) null else { ->
                                Text(account.issuer)
                            },
                            trailingContent = { OtpTypeBadge(account) },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = index,
                                count = state.accounts.size,
                                defaultShapes = ListItemDefaults.shapes(
                                    shape = if (state.accounts.isEmpty()) MaterialTheme.shapes.large else null
                                )
                            ),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                        ) {
                            Text(account.label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier.padding(start = 12.dp, bottom = 6.dp),
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AccountBadge(
    account: DomainExportAccount,
    modifier: Modifier = Modifier
) {
    val icon = account.icon
    Surface(
        modifier = modifier,
        shape = if (icon != null) {
            MaterialTheme.shapes.medium
        } else {
            MaterialShapes.Cookie4Sided.toShape()
        },
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                UriImage(uri = icon)
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
private fun OtpTypeBadge(
    account: DomainExportAccount,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val value = when (account) {
            is DomainExportAccount.Totp -> stringResource(R.string.export_account_period_value, account.period)
            is DomainExportAccount.Hotp -> account.counter.toString()
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge
        )

        val badgeRes = when (account) {
            is DomainExportAccount.Totp -> R.drawable.ic_timer
            is DomainExportAccount.Hotp -> R.drawable.ic_refresh
        }
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(id = badgeRes),
            contentDescription = null
        )

    }
}

@Composable
@PreviewAllConfigurations
private fun ExportScreen_Default_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ExportScreen(
                modifier = Modifier.fillMaxSize(),
                state = ExportScreenState.Success(
                    accounts = listOf(
                        DomainExportAccount.Totp(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                            icon = null,
                            label = "X1nto",
                            issuer = "GitHub",
                            period = 30
                        ),
                        DomainExportAccount.Hotp(
                            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                            icon = null,
                            label = "alice@example.com",
                            issuer = "Amazon Web Services",
                            counter = 42
                        )
                    ),
                    isAll = true
                ),
                onBackNavigate = {},
                onGoogleAuthQr = {},
                onMauthExport = {},
                onAegisExport = {},
                onFreeOtpPlusExport = {},
                onUriListExport = {},
                onAccountExport = {},
            )
        }
    }
}

@Composable
@PreviewAllConfigurations
private fun ExportScreen_Empty_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ExportScreen(
                modifier = Modifier.fillMaxSize(),
                state = ExportScreenState.Empty,
                onBackNavigate = {},
                onGoogleAuthQr = {},
                onMauthExport = {},
                onAegisExport = {},
                onFreeOtpPlusExport = {},
                onUriListExport = {},
                onAccountExport = {},
            )
        }
    }
}
