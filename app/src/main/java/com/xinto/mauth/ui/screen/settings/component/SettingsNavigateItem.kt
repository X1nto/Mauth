@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.settings.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.xinto.mauth.R

@Composable
fun SettingsNavigateItem(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    SettingsItem(
        onClick = onClick,
        modifier = modifier,
        shapes = shapes,
        icon = icon,
        description = description,
        title = title,
        trailing = {
            Icon(
                painter = painterResource(R.drawable.ic_navigate_next),
                contentDescription = null
            )
        },
        enabled = enabled
    )
}
