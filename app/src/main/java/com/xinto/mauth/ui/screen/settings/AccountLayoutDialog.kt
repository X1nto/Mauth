package com.xinto.mauth.ui.screen.settings

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.domain.settings.model.AccountsLayout

@Composable
fun AccountLayoutDialog(
    initialLayout: AccountsLayout,
    onConfirm: (AccountsLayout) -> Unit,
    onDismissRequest: () -> Unit
) {
    var layout by remember { mutableStateOf(initialLayout) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.accountslayout_title)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth()
            ) {
                AccountsLayout.entries.forEach {
                    val selected = layout == it
                    val interactionSource = remember { MutableInteractionSource() }
                    ListItem(
                        onClick = { layout = it },
                        interactionSource = interactionSource,
                        leadingContent = {
                            RadioButton(
                                selected = selected,
                                onClick = null,
                                interactionSource = interactionSource
                            )
                        },
                        selected = selected,
                        colors = ListItemDefaults.colors(
                            containerColor = AlertDialogDefaults.containerColor,
                            contentColor = AlertDialogDefaults.textContentColor,
                            selectedContainerColor = AlertDialogDefaults.containerColor,
                            selectedContentColor = AlertDialogDefaults.textContentColor
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(stringResource(it.labelRes))
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = { onConfirm(layout) }) {
                Text(stringResource(R.string.accountslayout_dialog_action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.accountslayout_dialog_action_cancel))
            }
        }
    )
}

