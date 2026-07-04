package com.xinto.mauth.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

@Composable
fun rememberMauthNavigator(
    initial: MauthDestination,
    isProtected: () -> Boolean,
): MauthNavigator {
    val backStack = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() },
        )
    ) {
        mutableStateListOf(initial)
    }
    return remember(backStack) { MauthNavigator(backStack, isProtected) }
}

@Stable
class MauthNavigator(
    val backStack: SnapshotStateList<MauthDestination>,
    private val isProtected: () -> Boolean,
) {

    fun navigate(destination: MauthDestination) {
        backStack.add(destination)
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
        if (isProtected()) {
            backStack.add(MauthDestination.Auth(nextDestination = destination))
        } else {
            backStack.add(destination)
        }
    }
}
