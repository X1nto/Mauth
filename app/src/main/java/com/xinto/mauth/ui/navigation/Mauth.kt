package com.xinto.mauth.ui.navigation

import com.xinto.mauth.ui.viewmodel.AddAccountParams
import com.xinto.taxi.BackstackNavigator
import com.xinto.taxi.Destination
import kotlinx.parcelize.Parcelize

typealias MauthNavigator = BackstackNavigator<Mauth>

sealed interface Mauth : Destination {
    @Parcelize
    object Home : Mauth

    @Parcelize
    object QrScanner : Mauth

    @Parcelize
    object QrSelector: Mauth

    @Parcelize
    class AddAccount(
        val params: AddAccountParams
    ): Mauth

    @Parcelize
    object Settings : Mauth
}