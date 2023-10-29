package com.xinto.mauth.ui.screen.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val colors = ListItemDefaults.colors(
        headlineColor = MaterialTheme.colorScheme.onSurface.let {
            if (!enabled) it.copy(alpha = 0.3f) else it
        },
        leadingIconColor = MaterialTheme.colorScheme.onSurface.let {
            if (!enabled) it.copy(alpha = 0.38f) else it
        },
        trailingIconColor = MaterialTheme.colorScheme.onSurface.let {
            if (!enabled) it.copy(alpha = 0.38f) else it
        },
    )
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .then(modifier),
        leadingContent = icon,
        trailingContent = trailing,
        supportingContent = description,
        headlineContent = title,
        tonalElevation = 1.dp,
        colors = colors,
    )
}

@Composable
fun SettingsItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
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
        trailing = trailing,
        enabled = enabled,
    )
}