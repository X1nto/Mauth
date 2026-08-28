package com.xinto.mauth.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.xinto.mauth.domain.settings.model.ColorScheme
import com.xinto.mauth.domain.settings.model.Font
import com.xinto.mauth.domain.settings.model.Theme
import com.xinto.mauth.ui.theme.color.BlueberryBlueDark
import com.xinto.mauth.ui.theme.color.LimeGreenDark
import com.xinto.mauth.ui.theme.color.MothPurpleDark
import com.xinto.mauth.ui.theme.color.OrangeOrangeDark
import com.xinto.mauth.ui.theme.color.SkyCyanDark
import com.xinto.mauth.ui.theme.color.LemonYellowDark
import com.xinto.mauth.ui.theme.color.BlueberryBlueLight
import com.xinto.mauth.ui.theme.color.LemonYellowLight
import com.xinto.mauth.ui.theme.color.LimeGreenLight
import com.xinto.mauth.ui.theme.color.MothPurpleLight
import com.xinto.mauth.ui.theme.color.OrangeOrangeLight
import com.xinto.mauth.ui.theme.color.SkyCyanLight

@Composable
fun MauthTheme(
    theme: Theme = Theme.DEFAULT,
    color: ColorScheme = ColorScheme.DEFAULT,
    font: Font = Font.DEFAULT,
    content: @Composable () -> Unit
) {
    val isDark = when (theme) {
        Theme.System -> isSystemInDarkTheme()
        Theme.Dark -> true
        Theme.Light -> false
    }
    val isInPreview = LocalInspectionMode.current
    val colorScheme = when {
        color == ColorScheme.Dynamic && (isInPreview || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            val context = LocalContext.current
            when (isDark) {
                true -> dynamicDarkColorScheme(context)
                false -> dynamicLightColorScheme(context)
            }
        }
        color == ColorScheme.BlueberryBlue -> when (isDark) {
            true -> BlueberryBlueDark
            false -> BlueberryBlueLight
        }
        color == ColorScheme.PickleYellow -> when (isDark) {
            true -> LemonYellowDark
            false -> LemonYellowLight
        }
        color == ColorScheme.ToxicGreen -> when (isDark) {
            true -> LimeGreenDark
            false -> LimeGreenLight
        }
        color == ColorScheme.LeatherOrange -> when (isDark) {
            true -> OrangeOrangeDark
            false -> OrangeOrangeLight
        }
        color == ColorScheme.OceanTurquoise -> when (isDark) {
            true -> SkyCyanDark
            false -> SkyCyanLight
        }
        else -> when (isDark) {
            true -> MothPurpleDark
            false -> MothPurpleLight
        }
    }
    val typography = when (font) {
        Font.Roboto -> DefaultTypography
        Font.GoogleSans -> GoogleSansTypography
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}