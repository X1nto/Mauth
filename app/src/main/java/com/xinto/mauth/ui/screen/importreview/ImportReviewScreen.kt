@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.importreview

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.ImportFailure
import com.xinto.mauth.core.backup.BackupFormat
import com.xinto.mauth.core.otp.OtpDigest
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.backup.model.DomainImportCandidate
import com.xinto.mauth.domain.backup.model.DomainRejectedEntry
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.preview.Screenshot
import com.xinto.mauth.ui.screen.home.OtpCode
import com.xinto.mauth.ui.screen.importer.rejectionReasonRes
import com.xinto.mauth.ui.screen.importer.titleRes
import com.xinto.mauth.ui.theme.MauthTheme
import com.xinto.mauth.ui.util.collectAsStateListWithLifecycle
import com.xinto.mauth.ui.util.collectAsStateMapWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Composable
fun ImportReviewScreen(
    source: BackupFormat,
    candidates: List<DomainImportCandidate>,
    rejected: List<DomainRejectedEntry>,
    onBack: () -> Unit,
    onImported: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ImportReviewViewModel = koinViewModel {
        parametersOf(candidates)
    }
    val realtimeData = viewModel.realtimeData.collectAsStateMapWithLifecycle()
    val selected = viewModel.selected.collectAsStateListWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(state) {
        (state as? ImportReviewState.Imported)?.let {
            onImported(it.count)
        }
        onPauseOrDispose {}
    }

    BackHandler(enabled = state == ImportReviewState.Importing || state is ImportReviewState.Imported) {}

    ImportReviewScreen(
        modifier = modifier,
        state = state,
        source = source,
        accounts = viewModel.accounts,
        realtimeData = realtimeData,
        selected = selected,
        rejected = rejected,
        duplicates = viewModel.duplicates,
        onBack = onBack,
        onCheckedChange = viewModel::setChecked,
        onImport = viewModel::commit,
        onDismissFailure = viewModel::dismissFailure
    )
}

@Composable
fun ImportReviewScreen(
    state: ImportReviewState,
    source: BackupFormat,
    accounts: ImmutableList<DomainAccount>,
    realtimeData: SnapshotStateMap<UUID, DomainOtpRealtimeData>,
    selected: SnapshotStateList<UUID>,
    rejected: List<DomainRejectedEntry>,
    onBack: () -> Unit,
    onCheckedChange: (UUID, Boolean) -> Unit,
    onImport: () -> Unit,
    onDismissFailure: () -> Unit,
    modifier: Modifier = Modifier,
    duplicates: ImmutableSet<UUID> = persistentSetOf(),
) {
    val importing = state == ImportReviewState.Importing || state is ImportReviewState.Imported
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.importreview_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !importing
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (importing) {
                        LoadingIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(40.dp)
                        )
                    } else {
                        TextButton(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = onImport,
                            enabled = selected.isNotEmpty()
                        ) {
                            Text(stringResource(R.string.importreview_action_import))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .wrapContentWidth()
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item(key = "hint", contentType = "hint") {
                ImportHintCard(
                    modifier = Modifier.padding(bottom = 8.dp),
                    source = source
                )
            }

            itemsIndexed(
                items = accounts,
                key = { _, account -> account.id },
                contentType = { _, _ -> "account" }
            ) { index, account ->
                ImportAccountItem(
                    account = account,
                    realtimeData = realtimeData[account.id],
                    checked = account.id in selected,
                    duplicate = account.id in duplicates,
                    onCheckedChange = { onCheckedChange(account.id, it) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = accounts.size
                    )
                )
            }

            if (rejected.isNotEmpty()) {
                item(key = "rejected-header", contentType = "header") {
                    Text(
                        modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 6.dp),
                        text = stringResource(R.string.importreview_rejected_header),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                itemsIndexed(
                    items = rejected,
                    contentType = { _, _ -> "rejected" }
                ) { index, entry ->
                    val issuer = if (entry.name.isNotEmpty()) entry.issuer else ""
                    ListItem(
                        modifier = Modifier.fillMaxWidth(),
                        leadingContent = {
                            AccountMonogram(
                                text = entry.shortLabel.ifEmpty { "?" },
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        overlineContent = if (issuer.isBlank()) null else { -> Text(issuer) },
                        supportingContent = { Text(stringResource(entry.reason.rejectionReasonRes)) },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = rejected.size
                        ),
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.name
                                .ifEmpty { entry.issuer }
                                .ifEmpty { stringResource(R.string.importreview_rejected_unnamed) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (state == ImportReviewState.Failed) {
        ImportFailedDialog(
            onRetry = onImport,
            onDismiss = onDismissFailure
        )
    }
}

@Composable
private fun ImportFailedDialog(
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.import_failed_title)) },
        text = { Text(stringResource(R.string.importreview_failed_message)) },
        confirmButton = {
            FilledTonalButton(onClick = onRetry) {
                Text(stringResource(R.string.importreview_failed_retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.importreview_failed_cancel))
            }
        }
    )
}

@Composable
private fun ImportHintCard(
    source: BackupFormat,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_info_filled),
                contentDescription = null
            )
            Text(
                text = if (source in BackupFormat.appFormats) {
                    stringResource(R.string.importreview_verify_hint_app, stringResource(source.titleRes))
                } else {
                    stringResource(R.string.importreview_verify_hint)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ImportAccountItem(
    account: DomainAccount,
    realtimeData: DomainOtpRealtimeData?,
    checked: Boolean,
    duplicate: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier
) {
    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ListItem(
        modifier = modifier.fillMaxWidth(),
        checked = checked,
        onCheckedChange = onCheckedChange,
        leadingContent = { AccountMonogram(text = account.shortLabel) },
        overlineContent = if (account.issuer.isBlank()) null else { -> Text(account.issuer) },
        supportingContent = if (!duplicate) null else { -> Text(stringResource(R.string.importreview_duplicate)) },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (realtimeData != null) {
                    OtpCode(
                        code = realtimeData.code,
                        visible = true,
                        progress = (realtimeData as? DomainOtpRealtimeData.Totp)?.progress
                    )
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = null
                )
            }
        },
        shapes = shapes.copy(selectedShape = shapes.shape),
        colors = colors.copy(
            selectedContainerColor = colors.containerColor,
            selectedContentColor = colors.contentColor,
            selectedLeadingContentColor = colors.leadingContentColor,
            selectedTrailingContentColor = colors.trailingContentColor,
            selectedOverlineContentColor = colors.overlineContentColor,
            selectedSupportingContentColor = colors.supportingContentColor
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = account.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AccountMonogram(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val style = MaterialTheme.typography.titleMedium
    Surface(
        modifier = modifier,
        shape = MaterialShapes.Cookie4Sided.toShape(),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = text,
                style = style,
                maxLines = 1,
                softWrap = false,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = style.fontSize
                )
            )
        }
    }
}

private val PreviewGithub = DomainAccount.Totp(
    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
    icon = null,
    secret = "JBSWY3DPEHPK3PXP",
    label = "X1nto",
    issuer = "GitHub",
    algorithm = OtpDigest.SHA1,
    digits = 6,
    createdMillis = 0L,
    period = 30,
)

private val PreviewFortinet = DomainAccount.Hotp(
    id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
    icon = null,
    secret = "JBSWY3DPEHPK3PXP",
    label = "alex@fortinet.com",
    issuer = "Fortinet",
    algorithm = OtpDigest.SHA1,
    digits = 6,
    createdMillis = 0L,
)

private val PreviewAws = DomainAccount.Totp(
    id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
    icon = null,
    secret = "JBSWY3DPEHPK3PXP",
    label = "alice@example.com",
    issuer = "Amazon Web Services",
    algorithm = OtpDigest.SHA1,
    digits = 6,
    createdMillis = 0L,
    period = 30,
)

private val PreviewAccounts = persistentListOf(PreviewGithub, PreviewFortinet, PreviewAws)

private val PreviewInvalidSecret = DomainRejectedEntry(
    name = "broken@example.com",
    issuer = "Example",
    reason = ImportFailure.InvalidSecret
)

private val PreviewRejected = listOf(
    PreviewInvalidSecret,
    DomainRejectedEntry(
        name = "carol",
        issuer = "Steam",
        reason = ImportFailure.Unrecognized
    ),
    DomainRejectedEntry(
        name = "",
        issuer = "",
        reason = ImportFailure.MissingName
    )
)

private val PreviewRealtimeData = mutableStateMapOf(
    PreviewGithub.id to DomainOtpRealtimeData.Totp(code = "123456", progress = 0.6f, countdown = 18),
    PreviewFortinet.id to DomainOtpRealtimeData.Hotp(code = "654321", count = 3),
    PreviewAws.id to DomainOtpRealtimeData.Totp(code = "424242", progress = 0.3f, countdown = 9)
)

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportReviewScreen_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ImportReviewScreen(
                modifier = Modifier.fillMaxSize(),
                state = ImportReviewState.Reviewing,
                source = BackupFormat.Proton,
                accounts = PreviewAccounts,
                realtimeData = PreviewRealtimeData,
                selected = remember { mutableStateListOf(PreviewGithub.id, PreviewAws.id) },
                rejected = PreviewRejected,
                onBack = {},
                onCheckedChange = { _, _ -> },
                onImport = {},
                onDismissFailure = {},
                duplicates = persistentSetOf(PreviewFortinet.id)
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportReviewScreen_Importing_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ImportReviewScreen(
                modifier = Modifier.fillMaxSize(),
                state = ImportReviewState.Importing,
                source = BackupFormat.Proton,
                accounts = PreviewAccounts,
                realtimeData = PreviewRealtimeData,
                selected = remember { mutableStateListOf(PreviewGithub.id, PreviewAws.id) },
                rejected = PreviewRejected,
                onBack = {},
                onCheckedChange = { _, _ -> },
                onImport = {},
                onDismissFailure = {},
                duplicates = persistentSetOf(PreviewFortinet.id)
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportReviewScreen_Failed_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ImportReviewScreen(
                modifier = Modifier.fillMaxSize(),
                state = ImportReviewState.Failed,
                source = BackupFormat.Proton,
                accounts = PreviewAccounts,
                realtimeData = PreviewRealtimeData,
                selected = remember { mutableStateListOf(PreviewGithub.id, PreviewAws.id) },
                rejected = PreviewRejected,
                onBack = {},
                onCheckedChange = { _, _ -> },
                onImport = {},
                onDismissFailure = {},
                duplicates = persistentSetOf(PreviewFortinet.id)
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportReviewScreen_Partial_Preview() {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ImportReviewScreen(
                modifier = Modifier.fillMaxSize(),
                state = ImportReviewState.Reviewing,
                source = BackupFormat.Aegis,
                accounts = persistentListOf(PreviewGithub),
                realtimeData = PreviewRealtimeData,
                selected = remember { mutableStateListOf(PreviewGithub.id) },
                rejected = listOf(PreviewInvalidSecret),
                onBack = {},
                onCheckedChange = { _, _ -> },
                onImport = {},
                onDismissFailure = {}
            )
        }
    }
}
