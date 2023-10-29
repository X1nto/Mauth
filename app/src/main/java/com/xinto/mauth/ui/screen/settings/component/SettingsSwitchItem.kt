package com.xinto.mauth.ui.screen.settings.component

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsSwitchItem(
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit)?,
    checked: Boolean,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val toggleableModifier = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            onValueChange = onCheckedChange
        )
    } else Modifier

    SettingsItem(
        modifier = modifier
            .then(toggleableModifier),
        icon = icon,
        description = description,
        title = title,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                thumbContent = thumbContent
            )
        },
        enabled = enabled
    )
}