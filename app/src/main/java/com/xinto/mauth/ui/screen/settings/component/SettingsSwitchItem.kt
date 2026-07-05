@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.settings.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState

@Composable
fun SettingsSwitchItem(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val switch: @Composable () -> Unit = {
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            thumbContent = thumbContent
        )
    }
    if (onCheckedChange != null) {
        SettingsItem(
            onClick = { onCheckedChange(!checked) },
            modifier = modifier.semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
            },
            shapes = shapes,
            icon = icon,
            description = description,
            title = title,
            trailing = switch,
            enabled = enabled,
        )
    } else {
        SettingsItem(
            modifier = modifier,
            shapes = shapes,
            icon = icon,
            description = description,
            title = title,
            trailing = switch,
            enabled = enabled,
        )
    }
}
