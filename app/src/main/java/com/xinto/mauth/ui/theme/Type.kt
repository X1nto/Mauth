package com.xinto.mauth.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.xinto.mauth.R

val DefaultTypography = Typography()
val GoogleSansTypography = FontFamily(Font(resId = R.font.google_sans)).typography()

fun FontFamily.typography(): Typography {
    return Typography(
        displayLarge = DefaultTypography.displayLarge.copy(fontFamily = this),
        displayLargeEmphasized = DefaultTypography.displayLargeEmphasized.copy(fontFamily = this),
        displayMedium = DefaultTypography.displayMedium.copy(fontFamily = this),
        displayMediumEmphasized = DefaultTypography.displayMediumEmphasized.copy(fontFamily = this),
        displaySmall = DefaultTypography.displaySmall.copy(fontFamily = this),
        displaySmallEmphasized = DefaultTypography.displaySmallEmphasized.copy(fontFamily = this),

        headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = this),
        headlineLargeEmphasized = DefaultTypography.headlineLargeEmphasized.copy(fontFamily = this),
        headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = this),
        headlineMediumEmphasized = DefaultTypography.headlineMediumEmphasized.copy(fontFamily = this),
        headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = this),
        headlineSmallEmphasized = DefaultTypography.headlineSmallEmphasized.copy(fontFamily = this),

        titleLarge = DefaultTypography.titleLarge.copy(fontFamily = this),
        titleLargeEmphasized = DefaultTypography.titleLargeEmphasized.copy(fontFamily = this),
        titleMedium = DefaultTypography.titleMedium.copy(fontFamily = this),
        titleMediumEmphasized = DefaultTypography.titleMediumEmphasized.copy(fontFamily = this),
        titleSmall = DefaultTypography.titleSmall.copy(fontFamily = this),
        titleSmallEmphasized = DefaultTypography.titleSmallEmphasized.copy(fontFamily = this),

        bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = this),
        bodyLargeEmphasized = DefaultTypography.bodyLargeEmphasized.copy(fontFamily = this),
        bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = this),
        bodyMediumEmphasized = DefaultTypography.bodyMediumEmphasized.copy(fontFamily = this),
        bodySmall = DefaultTypography.bodySmall.copy(fontFamily = this),
        bodySmallEmphasized = DefaultTypography.bodySmallEmphasized.copy(fontFamily = this),

        labelLarge = DefaultTypography.labelLarge.copy(fontFamily = this),
        labelLargeEmphasized = DefaultTypography.labelLargeEmphasized.copy(fontFamily = this),
        labelMedium = DefaultTypography.labelMedium.copy(fontFamily = this),
        labelMediumEmphasized = DefaultTypography.labelMediumEmphasized.copy(fontFamily = this),
        labelSmall = DefaultTypography.labelSmall.copy(fontFamily = this),
        labelSmallEmphasized = DefaultTypography.labelSmallEmphasized.copy(fontFamily = this)
    )
}