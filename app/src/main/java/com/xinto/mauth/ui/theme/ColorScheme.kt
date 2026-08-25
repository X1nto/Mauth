package com.xinto.mauth.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.luminance

val ColorScheme.isDark: Boolean
    get() = surface.luminance() < 0.5f
