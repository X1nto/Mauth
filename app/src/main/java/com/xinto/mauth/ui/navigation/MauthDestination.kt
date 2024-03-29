package com.xinto.mauth.ui.navigation

import android.os.Parcelable
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed class MauthDestination(val isFullscreenDialog: Boolean = false) : Parcelable {

    @Parcelize
    data object Auth : MauthDestination()

    @Parcelize
    data object Home : MauthDestination()

    @Parcelize
    data object QrScanner : MauthDestination()

    @Parcelize
    data class AddAccount(
        val params: DomainAccountInfo
    ) : MauthDestination(isFullscreenDialog = true)

    @Parcelize
    data class EditAccount(
        val id: UUID,
    ) : MauthDestination(isFullscreenDialog = true)

    @Parcelize
    data object Settings : MauthDestination()

    @Parcelize
    data object PinSetup : MauthDestination()

    @Parcelize
    data object PinRemove : MauthDestination()

    @Parcelize
    data object Theme : MauthDestination()

    @Parcelize
    data object About : MauthDestination()
}