package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.ui.screen.pinremove.PinRemoveScreen
import com.xinto.mauth.ui.screen.pinremove.PinRemoveScreenState
import com.xinto.mauth.ui.screen.pinsetup.PinSetupScreen
import com.xinto.mauth.ui.screen.pinsetup.PinSetupScreenState

class PinSetupScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun SetupMismatch() = ScreenshotSurface {
        PinSetupScreen(
            modifier = Modifier.fillMaxSize(),
            code = "1234",
            state = PinSetupScreenState.Confirm,
            error = true,
            onNext = {},
            onPrevious = {},
            onNumberEnter = {},
            onNumberDelete = {},
            onAllDelete = {}
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun RemoveWrongCode() = ScreenshotSurface {
        PinRemoveScreen(
            modifier = Modifier.fillMaxSize(),
            state = PinRemoveScreenState.Error("123"),
            onEnter = {},
            onBack = {},
            onNumberEnter = {},
            onNumberDelete = {},
            onAllDelete = {}
        )
    }
}
