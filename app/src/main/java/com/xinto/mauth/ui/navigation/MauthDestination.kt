package com.xinto.mauth.ui.navigation

import com.xinto.taxi.BackstackNavigator
import com.xinto.taxi.Destination
import kotlinx.parcelize.Parcelize

typealias MauthNavigator = BackstackNavigator<MauthDestination>

sealed class MauthDestination(val isFullscreenDialog: Boolean = false) : Destination {
    @Parcelize
    object Home : MauthDestination()

    @Parcelize
    object QrScanner : MauthDestination()

    @Parcelize
    class AddAccount(
        val params: AddAccountParams
    ): MauthDestination(isFullscreenDialog = true)

    @Parcelize
    object Settings : MauthDestination()
}