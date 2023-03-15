package com.xinto.mauth.ui.screen.account.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

@Composable
fun AccountExitDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(stringResource(R.string.addeditaccount_discard_title))
        },
        text = {
            Text(stringResource(R.string.addeditaccount_discard_subtitle))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.addeditaccount_discard_buttons_discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.addeditaccount_discard_buttons_cancel))
            }
        }
    )
}