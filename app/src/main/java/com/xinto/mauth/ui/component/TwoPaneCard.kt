package com.xinto.mauth.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
) {
    val shape by animateRoundedCornerShapeAsState(
        targetValue = if (selected) MaterialTheme.shapes.small else MaterialTheme.shapes.large,
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