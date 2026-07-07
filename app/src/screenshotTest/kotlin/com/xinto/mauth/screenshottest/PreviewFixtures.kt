package com.xinto.mauth.screenshottest

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.ui.theme.MauthTheme
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

@Composable
internal fun ScreenshotSurface(content: @Composable () -> Unit) {
    MauthTheme {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}

internal object PreviewFixtures {

    val github = DomainAccount.Totp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "X1nto",
        issuer = "GitHub",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
        period = 30,
    )

    val fortinet = DomainAccount.Hotp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "alex@fortinet.com",
        issuer = "Fortinet",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
    )

    val aws = DomainAccount.Totp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "alice@example.com",
        issuer = "Amazon Web Services",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
        period = 30,
    )

    val accounts = persistentListOf(github, fortinet, aws)

    val groups = persistentListOf(
        DomainGroup(
            id = UUID.fromString("00000000-0000-0000-0000-0000000000a1"),
            name = "Work",
            emoji = "💼",
            sortIndex = 0,
        ),
        DomainGroup(
            id = UUID.fromString("00000000-0000-0000-0000-0000000000a2"),
            name = "Personal",
            emoji = null,
            sortIndex = 1,
        ),
    )

    val addAccountInfo = DomainAccountInfo(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        icon = null,
        label = "Google",
        issuer = "google.com",
        secret = "JBSWY3DPEHPK3PXP",
        algorithm = OtpDigest.SHA1,
        type = OtpType.TOTP,
        digits = 6,
        counter = 0,
        period = 30,
        groupId = null,
        createdMillis = 0L,
    )

    /** Remembered realtime OTP codes for [accounts]; codes render verbatim (no live generation). */
    @Composable
    fun rememberRealtimeData() = remember {
        mutableStateMapOf(
            github.id to DomainOtpRealtimeData.Totp(code = "123456", progress = 0.6f, countdown = 18),
            fortinet.id to DomainOtpRealtimeData.Hotp(code = "654321", count = 3),
            aws.id to DomainOtpRealtimeData.Totp(code = "424242", progress = 0.3f, countdown = 9),
        )
    }
}
