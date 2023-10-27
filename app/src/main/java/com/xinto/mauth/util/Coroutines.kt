package com.xinto.mauth.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

inline fun <T> Flow<T>.catchMap(
    crossinline transformation: (Throwable) -> T
): Flow<T> {
    return catch {
        emit(transformation(it))
    }
}

fun <T> Flow<T>.launchInLifecycle(
    lifecycle: Lifecycle,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job {
    return flowWithLifecycle(lifecycle, state)
        .onEach(action)
        .launchIn(lifecycle.coroutineScope)
}