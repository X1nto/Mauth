package com.xinto.mauth.ui.screen.importer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xinto.mauth.R
import com.xinto.mauth.R.drawable.ic_error
import com.xinto.mauth.core.backup.importer.ImportFailure
import com.xinto.mauth.core.backup.importer.ImportSource
import com.xinto.mauth.ui.preview.PreviewAllConfigurations
import com.xinto.mauth.ui.preview.Screenshot
import com.xinto.mauth.ui.theme.MauthTheme

@Composable
fun ImportAttemptDialog(
    attempt: ImportAttempt,
    onPassword: (CharArray) -> Unit,
    onRetryAs: (ImportSource) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (attempt) {
        is ImportAttempt.Working -> {
            WorkingDialog(modifier = modifier)
        }
        is ImportAttempt.AwaitingPassword -> {
            ImportPasswordDialog(
                modifier = modifier,
                source = attempt.source,
                wrongPassword = attempt.wrongPassword,
                onConfirm = onPassword,
                onDismissRequest = onDismiss
            )
        }
        is ImportAttempt.WrongFormat -> {
            WrongFormatDialog(
                modifier = modifier,
                expected = attempt.expected,
                actual = attempt.actual,
                onRetryAs = onRetryAs,
                onDismiss = onDismiss
            )
        }
        is ImportAttempt.Failed -> {
            val messageRes = when (attempt.reason) {
                ImportFailure.Unrecognized -> R.string.import_failure_unrecognized
                ImportFailure.WrongPassword -> R.string.import_failure_wrongpassword
                ImportFailure.Malformed -> R.string.import_failure_malformed
                ImportFailure.UnsupportedVersion -> R.string.import_failure_unsupportedversion
                ImportFailure.InvalidSecret -> R.string.import_rejected_reason_invalidsecret
                ImportFailure.MissingName -> R.string.import_rejected_reason_missingname
            }
            FailedDialog(
                modifier = modifier,
                message = stringResource(messageRes),
                onDismiss = onDismiss
            )
        }
        is ImportAttempt.NothingToImport -> {
            FailedDialog(
                modifier = modifier,
                message = if (attempt.rejectedCount == 0) {
                    stringResource(R.string.import_empty)
                } else {
                    pluralStringResource(R.plurals.import_none_importable, attempt.rejectedCount, attempt.rejectedCount)
                },
                onDismiss = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WorkingDialog(modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        icon = { LoadingIndicator() },
        title = { Text(stringResource(R.string.import_working_title)) },
        text = { Text(stringResource(R.string.import_working_subtitle)) },
        confirmButton = {}
    )
}

@Composable
private fun WrongFormatDialog(
    expected: ImportSource,
    actual: ImportSource,
    onRetryAs: (ImportSource) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(ic_error),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.import_wrongformat_title)) },
        text = {
            Text(
                text = stringResource(R.string.import_wrongformat_message,
                    stringResource(actual.titleRes),
                    stringResource(expected.titleRes)
                )
            )
        },
        confirmButton = {
            FilledTonalButton(onClick = { onRetryAs(actual) }) {
                Text(stringResource(R.string.import_wrongformat_confirm, stringResource(actual.titleRes)))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.import_wrongformat_cancel))
            }
        }
    )
}

@Composable
private fun FailedDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(ic_error),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.import_failed_title)) },
        text = { Text(message) },
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.import_failed_action))
            }
        }
    )
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_Working_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.Working,
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_AwaitingPassword_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.AwaitingPassword(
                    source = ImportSource.Mauth,
                    wrongPassword = false
                ),
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_WrongPassword_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.AwaitingPassword(
                    source = ImportSource.Mauth,
                    wrongPassword = true
                ),
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_WrongFormat_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.WrongFormat(
                    expected = ImportSource.Mauth,
                    actual = ImportSource.TwoFas
                ),
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_Failed_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.Failed(ImportFailure.Unrecognized),
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_NothingToImport_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.NothingToImport(rejectedCount = 3),
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

@Composable
@Screenshot
@PreviewAllConfigurations
private fun ImportAttemptDialog_Empty_Preview() {
    MauthTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ImportAttemptDialog(
                attempt = ImportAttempt.NothingToImport(rejectedCount = 0),
                onPassword = {},
                onRetryAs = {},
                onDismiss = {}
            )
        }
    }
}

