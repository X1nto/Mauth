package com.xinto.mauth.ui.screen.settings

import androidx.annotation.StringRes
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.FontSetting
import com.xinto.mauth.core.settings.model.ThemeSetting

@get:StringRes
val FontSetting.labelRes: Int
    get() = when (this) {
        FontSetting.Roboto -> R.string.font_font_roboto
        FontSetting.GoogleSans -> R.string.font_font_google_sans
    }

@get:StringRes
val ThemeSetting.labelRes: Int
    get() = when (this) {
        ThemeSetting.System -> R.string.theme_theme_system
        ThemeSetting.Dark -> R.string.theme_theme_dark
        ThemeSetting.Light -> R.string.theme_theme_light
    }

@get:StringRes
val ColorSetting.labelRes: Int
    get() = when (this) {
        ColorSetting.Dynamic -> R.string.theme_colors_dynamic
        ColorSetting.MothPurple -> R.string.theme_colors_purple
        ColorSetting.BlueberryBlue -> R.string.theme_colors_blue
        ColorSetting.PickleYellow -> R.string.theme_colors_yellow
        ColorSetting.ToxicGreen -> R.string.theme_colors_green
        ColorSetting.LeatherOrange -> R.string.theme_colors_orange
        ColorSetting.OceanTurquoise -> R.string.theme_colors_turquoise
    }
