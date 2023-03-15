package com.xinto.mauth.ui.screen.qrscan.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

@Composable
fun QrScanPermissionDeniedDialog(
    shouldShowRationale: Boolean,
    onGrantPermission: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        text = {
            if (shouldShowRationale) {
                Text(stringResource(R.string.qrscan_permissions_subtitle_rationale))
            } else {
                Text(stringResource(R.string.qrscan_permissions_subtitle))
            }
        },
        confirmButton = {
            Button(onClick = onGrantPermission) {
                Text(stringResource(R.string.qrscan_permissions_button_grant))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(R.string.qrscan_permissions_button_cancel))
            }
        }
    )
}