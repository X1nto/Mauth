package com.xinto.mauth.ui.screen.export.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExportQrLayout(
    modifier: Modifier = Modifier,
    horizontalInset: Dp = 0.dp,
    spacing: Dp = 0.dp,
    footer: @Composable () -> Unit = {},
    code: @Composable () -> Unit,
) {
    Layout(
        contents = listOf(code, footer),
        modifier = modifier
    ) { (codeMeasurables, footerMeasurables), constraints ->
        val inset = horizontalInset.roundToPx() * 2
        val gap = if (footerMeasurables.isEmpty()) 0 else spacing.roundToPx()

        val maxSize = (constraints.maxWidth - inset).coerceAtLeast(0)
        val footerHeight = footerMeasurables.maxOfOrNull { it.minIntrinsicHeight(maxSize + inset) } ?: 0
        val size = if (constraints.hasBoundedHeight) {
            minOf(maxSize, constraints.maxHeight - footerHeight - gap).coerceAtLeast(0)
        } else {
            maxSize
        }

        val contentWidth = size + inset
        val codePlaceables = codeMeasurables.map { it.measure(Constraints.fixed(contentWidth, size)) }
        val footerPlaceables = footerMeasurables.map { it.measure(Constraints.fixedWidth(contentWidth)) }

        // The dialog may enforce a minimum width wider than the content, so center within it
        val width = contentWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val footerPlacedHeight = footerPlaceables.maxOfOrNull { it.height } ?: 0
        val height = (size + gap + footerPlacedHeight).coerceIn(constraints.minHeight, constraints.maxHeight)
        val x = (width - contentWidth) / 2

        layout(width, height) {
            codePlaceables.forEach { it.place(x, 0) }
            footerPlaceables.forEach { it.place(x, size + gap) }
        }
    }
}
