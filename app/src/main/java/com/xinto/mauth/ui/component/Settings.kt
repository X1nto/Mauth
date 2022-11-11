package com.xinto.mauth.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .then(modifier),
        leadingContent = icon,
        trailingContent = trailing,
        supportingText = description,
        headlineText = title,
        tonalElevation = 1.dp
    )
}

@Composable
fun SettingsItem(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    SettingsItem(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled
            ),
        icon = icon,
        description = description,
        title = title,
        trailing = trailing
    )
}

@Composable
fun SwitchSettingsItem(
    onCheckedChange: ((Boolean) -> Unit)?,
    checked: Boolean,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    thumbContent: (@Composable () -> Unit)? = null
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
        }
    )
}