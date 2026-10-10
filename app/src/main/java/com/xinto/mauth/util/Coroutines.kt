package com.xinto.mauth.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest

inline fun <T> Flow<T>.catchMap(
    crossinline transformation: (Throwable) -> T
): Flow<T> {
    return catch {
        emit(transformation(it))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T1, T2, R> combineTransformLatest(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    transform: suspend FlowCollector<R>.(T1, T2) -> Unit
): Flow<R> {
    return combine(flow, flow2, ::Pair).transformLatest { (a, b) ->
        transform(a, b)
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