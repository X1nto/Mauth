package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.ui.screen.account.AccountForm
import com.xinto.mauth.ui.screen.account.AccountScreen
import com.xinto.mauth.ui.screen.account.AccountScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class AccountScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun Loaded() = ScreenshotSurface {
        val form = remember {
            AccountForm(
                initial = PreviewFixtures.addAccountInfo,
                groups = MutableStateFlow(emptyList()),
                onCreateGroup = { _, _ -> UUID.randomUUID() },
            )
        }
        AccountScreen(
            modifier = Modifier.fillMaxSize(),
            title = "Add account",
            state = AccountScreenState.Success(form),
            onSave = {},
            onExit = {},
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun Loading() = ScreenshotSurface {
        AccountScreen(
            modifier = Modifier.fillMaxSize(),
            title = "Add account",
            state = AccountScreenState.Loading,
            onSave = {},
            onExit = {},
        )
    }
}
