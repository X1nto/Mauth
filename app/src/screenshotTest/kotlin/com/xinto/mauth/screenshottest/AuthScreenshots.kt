package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.ui.screen.auth.AuthScreen

class AuthScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun EmptyCode() = ScreenshotSurface {
        AuthScreen(
            modifier = Modifier.fillMaxSize(),
            code = "",
            onNumberAdd = {},
            onNumberDelete = {},
            onClear = {},
            showFingerprint = false,
            onFingerprintClick = {},
            onBackPress = null,
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun WithFingerprint() = ScreenshotSurface {
        AuthScreen(
            modifier = Modifier.fillMaxSize(),
            code = "",
            onNumberAdd = {},
            onNumberDelete = {},
            onClear = {},
            showFingerprint = true,
            onFingerprintClick = {},
            onBackPress = null,
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun WithCode() = ScreenshotSurface {
        AuthScreen(
            modifier = Modifier.fillMaxSize(),
            code = "123",
            onNumberAdd = {},
            onNumberDelete = {},
            onClear = {},
            showFingerprint = true,
            onFingerprintClick = {},
            onBackPress = null,
        )
    }
}
