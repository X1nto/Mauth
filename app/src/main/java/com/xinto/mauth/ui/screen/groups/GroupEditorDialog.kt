package com.xinto.mauth.ui.screen.groups

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import com.xinto.mauth.R
import androidx.emoji2.emojipicker.R as EmojiPickerR

@Composable
fun CreateGroupDialog(
    isNameTaken: (String) -> Boolean,
    onConfirm: (name: String, emoji: String?) -> Unit,
    onDismissRequest: () -> Unit
) {
    CreateEditGroupDialog(
        title = stringResource(R.string.groups_create_title),
        confirmText = stringResource(R.string.groups_dialog_action_create),
        initialName = "",
        initialEmoji = null,
        isNameTaken = isNameTaken,
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun EditGroupDialog(
    initialName: String,
    initialEmoji: String?,
    isNameTaken: (String) -> Boolean,
    onConfirm: (name: String, emoji: String?) -> Unit,
    onDismissRequest: () -> Unit
) {
    CreateEditGroupDialog(
        title = stringResource(R.string.groups_rename_title),
        confirmText = stringResource(R.string.groups_dialog_action_save),
        initialName = initialName,
        initialEmoji = initialEmoji,
        isNameTaken = isNameTaken,
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest
    )
}

@Composable
private fun CreateEditGroupDialog(
    title: String,
    confirmText: String,
    initialName: String,
    initialEmoji: String?,
    isNameTaken: (String) -> Boolean,
    onConfirm: (name: String, emoji: String?) -> Unit,
    onDismissRequest: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var emoji by remember { mutableStateOf(initialEmoji) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val trimmed = name.trim()
    val duplicate = isNameTaken(trimmed)
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.groups_dialog_field_name)) },
                singleLine = true,
                isError = duplicate,
                supportingText = if (!duplicate) null else { -> Text(stringResource(R.string.groups_dialog_error_duplicate)) },
                trailingIcon = {
                    val emojiLabel = stringResource(
                        if (emoji != null) R.string.groups_emoji_change else R.string.groups_emoji_add
                    )
                    TooltipBox(
                        modifier = Modifier,
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                        tooltip = { this.PlainTooltip { Text(text = emojiLabel) } },
                        state = rememberTooltipState(),
                        content = {
                            IconButton(onClick = { showEmojiPicker = true }) {
                                val current = emoji
                                if (current != null) {
                                    Text(
                                        text = current,
                                        fontSize = 22.sp,
                                        modifier = Modifier.semantics { contentDescription = emojiLabel }
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_add_reaction),
                                        contentDescription = emojiLabel
                                    )
                                }
                            }
                        },
                    )
                }
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onConfirm(trimmed, emoji) },
                enabled = trimmed.isNotEmpty() && !duplicate
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.groups_dialog_action_cancel))
            }
        }
    )
    if (showEmojiPicker) {
        EmojiPickerSheet(
            canRemove = emoji != null,
            onPick = {
                emoji = it
                showEmojiPicker = false
            },
            onRemove = {
                emoji = null
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

@SuppressLint("PrivateResource", "ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerSheet(
    canRemove: Boolean,
    onPick: (String) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val selectedTint = MaterialTheme.colorScheme.primary.toArgb()
    val unselectedTint = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = false,
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Expanded,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.groups_emoji_choose),
                style = MaterialTheme.typography.titleMedium
            )
            if (canRemove) {
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.groups_emoji_action_remove))
                }
            }
        }
        AndroidView(
            factory = { context ->
                val nightConfig = Configuration(context.resources.configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                        if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                }
                val themedContext = ContextThemeWrapper(
                    context.createConfigurationContext(nightConfig),
                    R.style.Theme_EmojiPicker
                )
                EmojiPickerView(themedContext).apply {
                    applyTheme(selectedTint, unselectedTint)
                    setOnEmojiPickedListener {
                        onPick(it.emoji)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .padding(top = 8.dp)
        )
    }
}

@SuppressLint("PrivateResource")
private fun EmojiPickerView.applyTheme(selectedColor: Int, unselectedColor: Int) {
    val tint = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
        intArrayOf(selectedColor, unselectedColor)
    )
    viewTreeObserver.addOnGlobalLayoutListener {
        val header = findViewById<ViewGroup>(EmojiPickerR.id.emoji_picker_header)
            ?: return@addOnGlobalLayoutListener
        for (i in 0 until header.childCount) {
            val item = header.getChildAt(i)
            val headerIcon = item.findViewById<ImageView>(EmojiPickerR.id.emoji_picker_header_icon)
            if (headerIcon != null && headerIcon.imageTintList !== tint) {
                headerIcon.imageTintList = tint
            }
            val headerUnderline = item.findViewById<View>(EmojiPickerR.id.emoji_picker_header_underline)
            if (headerUnderline != null && headerUnderline.backgroundTintList !== tint) {
                headerUnderline.backgroundTintList = tint
            }
        }
    }
}
