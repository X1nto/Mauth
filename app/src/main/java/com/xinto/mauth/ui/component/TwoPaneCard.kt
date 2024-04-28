package com.xinto.mauth.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TwoPaneCard(
    selected: Boolean,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape by animateRoundedCornerShapeAsState(
        targetValue = if (selected) MaterialTheme.shapes.small else MaterialTheme.shapes.large,
    )
    ElevatedCard(
        modifier = modifier,
        shape = shape,
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