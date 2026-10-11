package com.xinto.mauth.ui.screen.importer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.core.backup.BackupFormat

@Composable
fun ImportPasswordDialog(
    source: BackupFormat,
    wrongPassword: Boolean,
    onConfirm: (CharArray) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val passwordState = rememberTextFieldState()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_key),
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.import_unlock_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.import_unlock_subtitle, stringResource(source.titleRes)))
                OutlinedSecureTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = passwordState,
                    label = { Text(stringResource(R.string.export_file_input_password)) },
                    isError = wrongPassword,
                    supportingText = if (!wrongPassword) null else { ->
                        Text(stringResource(R.string.import_unlock_wrong))
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(passwordState.text.toString().toCharArray()) },
                enabled = passwordState.text.isNotEmpty()
            ) {
                Text(stringResource(R.string.import_unlock_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.export_file_action_cancel))
            }
        }
    )
}
