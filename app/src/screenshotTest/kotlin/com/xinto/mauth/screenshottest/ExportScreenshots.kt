package com.xinto.mauth.screenshottest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.xinto.mauth.domain.account.model.DomainExportAccount
import com.xinto.mauth.ui.screen.export.ExportScreen
import com.xinto.mauth.ui.screen.export.ExportScreenState
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

class ExportScreenshots {

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun ExportMethods() = ScreenshotSurface {
        ExportScreen(
            modifier = Modifier.fillMaxSize(),
            state = ExportScreenState.Success(
                accounts = listOf(
                    DomainExportAccount.Totp(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        icon = null,
                        label = "X1nto",
                        issuer = "GitHub",
                        period = 30
                    ),
                    DomainExportAccount.Totp(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                        icon = null,
                        label = "alex@fortinet.com",
                        issuer = "Fortinet",
                        period = 60
                    ),
                    DomainExportAccount.Hotp(
                        id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                        icon = null,
                        label = "alice@example.com",
                        issuer = "Amazon Web Services",
                        counter = 42
                    )
                ),
                isAll = true
            ),
            onBackNavigate = {},
            onGoogleAuthQr = {},
            onMauthExport = {},
            onAegisExport = {},
            onFreeOtpPlusExport = {},
            onUriListExport = {},
            onAccountExport = {}
        )
    }

    @PreviewTest
    @Composable
    @PreviewAllConfigurations
    fun ExportMethodsEmpty() = ScreenshotSurface {
        ExportScreen(
            modifier = Modifier.fillMaxSize(),
            state = ExportScreenState.Empty,
            onBackNavigate = {},
            onGoogleAuthQr = {},
            onMauthExport = {},
            onAegisExport = {},
            onFreeOtpPlusExport = {},
            onUriListExport = {},
            onAccountExport = {}
        )
    }
}
