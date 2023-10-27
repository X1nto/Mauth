package com.xinto.mauth.ui.screen.home.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

@Composable
fun HomeDeleteAccountsDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_delete_forever),
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
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.home_delete_button_cancel))
            }
        }
    )
}