package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.domain.settings.model.AccountsLayout
import com.xinto.mauth.domain.settings.model.ColorScheme
import com.xinto.mauth.domain.settings.model.Font
import com.xinto.mauth.domain.settings.model.Theme
import com.xinto.mauth.ui.screen.settings.SettingsScreen

class SettingsScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun AllOff() = ScreenshotSurface {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onBack = {},
            secureMode = false,
            onSecureModeChange = {},
            pinCode = false,
            onPinCodeChange = {},
            meshGradientBackground = false,
            onMeshGradientBackgroundChange = {},
            lockOnResume = false,
            onLockOnResumeChange = {},
            showBiometrics = false,
            biometrics = false,
            onBiometricsChange = {},
            onThemeNavigate = {},
            theme = Theme.DEFAULT,
            color = ColorScheme.MothPurple,
            font = Font.DEFAULT,
            onFontChange = {},
            accountsLayout = AccountsLayout.DEFAULT,
            onAccountsLayoutChange = {},
            showCodesByDefault = false,
            onShowCodesByDefaultChange = {}
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun Mixed() = ScreenshotSurface {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onBack = {},
            secureMode = false,
            onSecureModeChange = {},
            pinCode = true,
            onPinCodeChange = {},
            meshGradientBackground = false,
            onMeshGradientBackgroundChange = {},
            lockOnResume = false,
            onLockOnResumeChange = {},
            showBiometrics = true,
            biometrics = true,
            onBiometricsChange = {},
            onThemeNavigate = {},
            theme = Theme.DEFAULT,
            color = ColorScheme.MothPurple,
            font = Font.DEFAULT,
            onFontChange = {},
            accountsLayout = AccountsLayout.DEFAULT,
            onAccountsLayoutChange = {},
            showCodesByDefault = false,
            onShowCodesByDefaultChange = {}
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun AllOn() = ScreenshotSurface {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onBack = {},
            secureMode = true,
            onSecureModeChange = {},
            pinCode = true,
            onPinCodeChange = {},
            meshGradientBackground = false,
            onMeshGradientBackgroundChange = {},
            lockOnResume = true,
            onLockOnResumeChange = {},
            showBiometrics = true,
            biometrics = true,
            onBiometricsChange = {},
            onThemeNavigate = {},
            theme = Theme.DEFAULT,
            color = ColorScheme.MothPurple,
            font = Font.DEFAULT,
            onFontChange = {},
            accountsLayout = AccountsLayout.DEFAULT,
            onAccountsLayoutChange = {},
            showCodesByDefault = true,
            onShowCodesByDefaultChange = {}
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun WithoutBiometrics() = ScreenshotSurface {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onBack = {},
            secureMode = false,
            onSecureModeChange = {},
            pinCode = true,
            onPinCodeChange = {},
            meshGradientBackground = false,
            onMeshGradientBackgroundChange = {},
            lockOnResume = false,
            onLockOnResumeChange = {},
            showBiometrics = false,
            biometrics = false,
            onBiometricsChange = {},
            onThemeNavigate = {},
            theme = Theme.DEFAULT,
            color = ColorScheme.MothPurple,
            font = Font.DEFAULT,
            onFontChange = {},
            accountsLayout = AccountsLayout.DEFAULT,
            onAccountsLayoutChange = {},
            showCodesByDefault = false,
            onShowCodesByDefaultChange = {}
        )
    }
}
