package com.xinto.mauth.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun <A> dropUnlessResumed(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    block: (A) -> Unit,
): (A) -> Unit {
    val currentBlock by rememberUpdatedState(block)
    return remember(lifecycleOwner) {
        { a ->
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                currentBlock(a)
            }
        }
    }
}