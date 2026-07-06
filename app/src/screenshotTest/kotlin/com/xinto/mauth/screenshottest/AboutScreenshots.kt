package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.ui.screen.about.AboutScreen
import com.xinto.mauth.ui.screen.about.InstallSource

class AboutScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun About() = ScreenshotSurface {
        AboutScreen(
            modifier = Modifier.fillMaxSize(),
            onBack = {},
            versionName = "0.10.0",
            installSource = InstallSource.GooglePlay,
            onSourceClick = {},
            onFeedbackClick = {},
            onPrivacyClick = {},
            onLicenseClick = {},
            onCopyBuildInfo = {},
        )
    }
}
