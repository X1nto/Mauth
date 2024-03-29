package com.xinto.mauth.ui.screen.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.xinto.mauth.R

@Composable
fun SettingsNavigateItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    SettingsItem(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
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