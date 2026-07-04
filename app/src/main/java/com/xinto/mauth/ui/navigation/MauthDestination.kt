package com.xinto.mauth.ui.navigation

import android.os.Parcelable
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import kotlinx.parcelize.Parcelize
import java.util.UUID

sealed interface MauthDestination : Parcelable {

    val isFullscreenDialog: Boolean get() = false

    @Parcelize
    data class Auth(val nextDestination: MauthDestination? = null) : MauthDestination

    @Parcelize
    data object Home : MauthDestination

    @Parcelize
    data object QrScanner : MauthDestination

    @Parcelize
    data class AddAccount(val params: DomainAccountInfo) : MauthDestination {
        override val isFullscreenDialog: Boolean get() = true
    }

    @Parcelize
    data class EditAccount(val id: UUID) : MauthDestination {
        override val isFullscreenDialog: Boolean get() = true
    }

    @Parcelize
    data object Settings : MauthDestination

    @Parcelize
    data class Export(
        // Empty list means export all
        val accounts: List<UUID> = emptyList()
    ) : MauthDestination

    @Parcelize
    data object PinSetup : MauthDestination

    @Parcelize
    data object PinRemove : MauthDestination

    @Parcelize
    data object Theme : MauthDestination

    @Parcelize
    data object About : MauthDestination

    @Parcelize
    data object Groups : MauthDestination
}
