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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.FontSetting

@Composable
fun FontDialog(
    initialFont: FontSetting,
    onConfirm: (FontSetting) -> Unit,
    onDismissRequest: () -> Unit
) {
    var font by remember { mutableStateOf(initialFont) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.font_title)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth()
            ) {
                FontSetting.entries.forEach {
                    FontRow(
                        font = it,
                        isSelected = font == it,
                        onClick = { font = it }
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onConfirm(font) }
            ) {
                Text(stringResource(R.string.font_dialog_action_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.font_dialog_action_cancel))
            }
        }
    )
}

@Composable
private fun FontRow(
    font: FontSetting,
    isSelected: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    ListItem(
        onClick = onClick,
        interactionSource = interactionSource,
        leadingContent = {
            RadioButton(
                selected = isSelected,
                onClick = null,
                interactionSource = interactionSource
            )
        },
        selected = isSelected,
        colors = ListItemDefaults.colors(
            containerColor = AlertDialogDefaults.containerColor,
            contentColor = AlertDialogDefaults.textContentColor,
            selectedContainerColor = AlertDialogDefaults.containerColor,
            selectedContentColor = AlertDialogDefaults.textContentColor
        ),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        val textRes = remember(font) {
            when (font) {
                FontSetting.Roboto -> R.string.font_font_roboto
                FontSetting.GoogleSans -> R.string.font_font_google_sans
            }
        }
        Text(stringResource(textRes))
    }
}