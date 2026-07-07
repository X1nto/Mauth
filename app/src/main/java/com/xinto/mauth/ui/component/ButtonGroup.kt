@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.xinto.mauth.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

fun ButtonGroupScope.mediumClickableItem(
    onClick: () -> Unit,
    label: String,
    weight: Float = 1f,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    customItem(
        buttonGroupContent = {
            val interactionSource = remember { MutableInteractionSource() }
            Button(
                onClick = onClick,
                enabled = enabled,
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                modifier = Modifier
                    .weight(weight)
                    .animateWidth(interactionSource)
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
                interactionSource = interactionSource,
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.size(ButtonDefaults.MediumIconSize),
                        propagateMinConstraints = true
                    ) {
                        icon()
                    }
                    Spacer(Modifier.width(ButtonDefaults.MediumIconSpacing))
                }
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
        },
        menuContent = {}
    )
}
