package com.xinto.mauth.screenshot

import androidx.compose.runtime.mutableStateMapOf
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.group.model.DomainGroup
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

internal object StoreFixtures {

    private val githubAccount = DomainAccount.Totp(
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

    val discordAccount = DomainAccount.Totp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "Xinto",
        issuer = "Discord",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
        period = 30,
    )

    val hotpAccount = DomainAccount.Hotp(
        id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
        icon = null,
        secret = "JBSWY3DPEHPK3PXP",
        label = "Xinto",
        issuer = "Fortinet",
        algorithm = OtpDigest.SHA1,
        digits = 6,
        createdMillis = 0L,
    )

    val sampleAccounts = persistentListOf(githubAccount, discordAccount, hotpAccount)

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

    val sampleRealtimeData = mutableStateMapOf(
        githubAccount.id to DomainOtpRealtimeData.Totp(
            code = "123456",
            progress = 20f / 30f,
            countdown = 20
        ),
        discordAccount.id to DomainOtpRealtimeData.Totp(
            code = "424242",
            progress = 20f / 30f,
            countdown = 20
        ),
        hotpAccount.id to DomainOtpRealtimeData.Hotp(code = "654321", count = 10),
    )

    val addAccountInfo = DomainAccountInfo(
        id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
        icon = null,
        label = "X1nto",
        issuer = "GitHub",
        secret = "JBSWY3DPEHPK3PXP",
        algorithm = OtpDigest.SHA1,
        type = OtpType.TOTP,
        digits = 6,
        counter = 0,
        period = 30,
        groupId = null,
        createdMillis = 0L,
    )

}