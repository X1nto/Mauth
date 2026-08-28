package com.xinto.mauth.ui.screen.settings

import androidx.annotation.StringRes
import com.xinto.mauth.R
import com.xinto.mauth.domain.settings.model.AccountsLayout
import com.xinto.mauth.domain.settings.model.ColorScheme
import com.xinto.mauth.domain.settings.model.Font
import com.xinto.mauth.domain.settings.model.Theme

@get:StringRes
val Font.labelRes: Int
    get() = when (this) {
        Font.Roboto -> R.string.font_font_roboto
        Font.GoogleSans -> R.string.font_font_google_sans
    }

@get:StringRes
val Theme.labelRes: Int
    get() = when (this) {
        Theme.System -> R.string.theme_theme_system
        Theme.Dark -> R.string.theme_theme_dark
        Theme.Light -> R.string.theme_theme_light
    }

@get:StringRes
val ColorScheme.labelRes: Int
    get() = when (this) {
        ColorScheme.Dynamic -> R.string.theme_colors_dynamic
        ColorScheme.MothPurple -> R.string.theme_colors_purple
        ColorScheme.BlueberryBlue -> R.string.theme_colors_blue
        ColorScheme.PickleYellow -> R.string.theme_colors_yellow
        ColorScheme.ToxicGreen -> R.string.theme_colors_green
        ColorScheme.LeatherOrange -> R.string.theme_colors_orange
        ColorScheme.OceanTurquoise -> R.string.theme_colors_turquoise
    }

@get:StringRes
val AccountsLayout.labelRes: Int
    get() = when (this) {
        AccountsLayout.Cards -> R.string.accountslayout_layout_cards
        AccountsLayout.Compact -> R.string.accountslayout_layout_compact
    }
