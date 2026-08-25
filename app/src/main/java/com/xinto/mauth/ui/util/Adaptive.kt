package com.xinto.mauth.ui.util

import androidx.window.core.layout.WindowSizeClass

val WindowSizeClass.isWidthAtLeastMedium: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

val WindowSizeClass.isWidthAtLeastExpanded: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

val WindowSizeClass.isWidthAtLeastLarge: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

val WindowSizeClass.isWidthAtLeastExtraLarge: Boolean
    get() = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND)

val WindowSizeClass.isHeightAtLeastMedium: Boolean
    get() = isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

val WindowSizeClass.isHeightAtLeastExpanded: Boolean
    get() = isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
