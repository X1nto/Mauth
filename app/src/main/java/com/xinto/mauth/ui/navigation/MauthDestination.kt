package com.xinto.mauth.ui.navigation

import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.taxi.BackstackNavigator
import com.xinto.taxi.Destination
import kotlinx.parcelize.Parcelize
import java.util.*

typealias MauthNavigator = BackstackNavigator<MauthDestination>

sealed class MauthDestination(val isFullscreenDialog: Boolean = false) : Destination {
    @Parcelize
    object Home : MauthDestination()

    @Parcelize
    object QrScanner : MauthDestination()

    @Parcelize
    class AddAccount(
        val params: DomainAccountInfo
    ) : MauthDestination(isFullscreenDialog = true)

    @Parcelize
    class EditAccount(
        val id: UUID,
    ) : MauthDestination(isFullscreenDialog = true)

    @Parcelize
    object Settings : MauthDestination()
}