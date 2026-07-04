package com.xinto.mauth.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TwoPaneCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
    expanded: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val restingShape = if (selected) MaterialTheme.shapes.small else MaterialTheme.shapes.large
    val pressedShape = if (selected) MaterialTheme.shapes.large else MaterialTheme.shapes.small
    val shape by animateRoundedCornerShapeAsState(
        targetValue = if (isPressed) pressedShape else restingShape,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
    )
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(12.dp)
        ) {
            topContent()
            AnimatedVisibility(
                visible = expanded,
            ) {
                Column {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    bottomContent()
                }
            }
        }
    }
}