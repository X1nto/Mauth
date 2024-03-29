package com.xinto.mauth.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import com.xinto.mauth.ui.theme.color.DarkBlueberryBlue
import com.xinto.mauth.ui.theme.color.DarkLemonYellow
import com.xinto.mauth.ui.theme.color.DarkLimeGreen
import com.xinto.mauth.ui.theme.color.DarkMothPurple
import com.xinto.mauth.ui.theme.color.DarkOrangeOrange
import com.xinto.mauth.ui.theme.color.DarkSkyCyan
import com.xinto.mauth.ui.theme.color.LightBlueberryBlue
import com.xinto.mauth.ui.theme.color.LightLemonYellow
import com.xinto.mauth.ui.theme.color.LightLimeGreen
import com.xinto.mauth.ui.theme.color.LightMothPurple
import com.xinto.mauth.ui.theme.color.LightOrangeOrange
import com.xinto.mauth.ui.theme.color.LightSkyCyan

@Composable
fun MauthTheme(
    theme: ThemeSetting = ThemeSetting.DEFAULT,
    color: ColorSetting = ColorSetting.DEFAULT,
    content: @Composable () -> Unit
) {
    val isDark = when (theme) {
        ThemeSetting.System -> isSystemInDarkTheme()
        ThemeSetting.Dark -> true
        ThemeSetting.Light -> false
    }
    val isInPreview = LocalInspectionMode.current
    val colorScheme = when {
        color == ColorSetting.Dynamic && (isInPreview || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            val context = LocalContext.current
            when (isDark) {
                true -> dynamicDarkColorScheme(context)
                false -> dynamicLightColorScheme(context)
            }
        }
        color == ColorSetting.BlueberryBlue -> when (isDark) {
            true -> DarkBlueberryBlue
            false -> LightBlueberryBlue
        }
        color == ColorSetting.LemonYellow -> when (isDark) {
            true -> DarkLemonYellow
            false -> LightLemonYellow
        }
        color == ColorSetting.LimeGreen -> when (isDark) {
            true -> DarkLimeGreen
            false -> LightLimeGreen
        }
        color == ColorSetting.OrangeOrange -> when (isDark) {
            true -> DarkOrangeOrange
            false -> LightOrangeOrange
        }
        color == ColorSetting.SkyCyan -> when (isDark) {
            true -> DarkSkyCyan
            false -> LightSkyCyan
        }
        else -> when (isDark) {
            true -> DarkMothPurple
            false -> LightMothPurple
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}