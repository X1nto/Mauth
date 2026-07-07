@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.screen.settings.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse

@Composable
fun SettingsItem(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    colors: ListItemColors = SettingsItemDefaults.colors(),
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    enabled: Boolean = true,
) {
    ListItem(
        modifier = modifier,
        enabled = enabled,
        leadingContent = icon,
        trailingContent = trailing,
        supportingContent = description,
        shapes = shapes,
        colors = colors,
        verticalAlignment = Alignment.CenterVertically,
        content = title,
    )
}

@Composable
fun SettingsItem(
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    colors: ListItemColors = SettingsItemDefaults.colors(),
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    ListItem(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingContent = icon,
        trailingContent = trailing,
        supportingContent = description,
        shapes = shapes,
        colors = colors,
        verticalAlignment = Alignment.CenterVertically,
        content = title,
        interactionSource = interactionSource,
    )
}


object SettingsItemDefaults {

    @Composable
    fun colors(
        containerColor: Color = Color.Unspecified,
        titleColor: Color = Color.Unspecified,
        descriptionColor: Color = Color.Unspecified,
        iconColor: Color = Color.Unspecified,
        trailingColor: Color = Color.Unspecified,
        disabledContainerColor: Color = Color.Unspecified,
        disabledTitleColor: Color = Color.Unspecified,
        disabledDescriptionColor: Color = Color.Unspecified,
        disabledIconColor: Color = Color.Unspecified,
        disabledTrailingColor: Color = Color.Unspecified,
    ): ListItemColors {
        val containerColor = containerColor.takeOrElse { MaterialTheme.colorScheme.surfaceContainer }
        val titleColor = titleColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
        val descriptionColor = descriptionColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
        val iconColor = iconColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
        val trailingColor = trailingColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
        return ListItemColors(
            containerColor = containerColor,
            contentColor = titleColor,
            supportingContentColor = descriptionColor,
            leadingContentColor = iconColor,
            trailingContentColor = trailingColor,
            disabledContainerColor = disabledContainerColor.takeOrElse { MaterialTheme.colorScheme.surfaceContainerLow },
            disabledContentColor = disabledTitleColor.takeOrElse { MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) },
            disabledSupportingContentColor = disabledDescriptionColor.takeOrElse {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                )
            },
            disabledLeadingContentColor = disabledIconColor.takeOrElse {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            },
            disabledTrailingContentColor = disabledTrailingColor.takeOrElse {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            },
            overlineContentColor = Color.Unspecified,
            disabledOverlineContentColor = Color.Unspecified,
            selectedContainerColor = containerColor,
            selectedContentColor = titleColor,
            selectedLeadingContentColor = iconColor,
            selectedTrailingContentColor = trailingColor,
            selectedOverlineContentColor = Color.Unspecified,
            selectedSupportingContentColor = descriptionColor,
            draggedContainerColor = Color.Unspecified,
            draggedContentColor = Color.Unspecified,
            draggedLeadingContentColor = Color.Unspecified,
            draggedTrailingContentColor = Color.Unspecified,
            draggedOverlineContentColor = Color.Unspecified,
            draggedSupportingContentColor = Color.Unspecified
        )
    }

}