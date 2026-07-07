package com.xinto.mauth.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList

@Stable
class MauthNavigator(
    val backStack: SnapshotStateList<MauthDestination>,
    private val isProtected: () -> Boolean,
) {

    fun navigate(destination: MauthDestination) {
        if (backStack.lastOrNull() != destination) {
            backStack.add(destination)
        }
    }

    fun pop() {
        backStack.removeLastOrNull()
    }

    fun replaceLast(destination: MauthDestination) {
        if (backStack.isEmpty()) {
            backStack.add(destination)
        } else {
            backStack[backStack.lastIndex] = destination
        }
    }

    fun replaceAll(destination: MauthDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    fun navigateSecure(destination: MauthDestination) {
        val target = if (isProtected()) {
            MauthDestination.Auth(nextDestination = destination)
        } else {
            destination
        }
        if (backStack.lastOrNull() != target) {
            backStack.add(target)
        }
    }
}
