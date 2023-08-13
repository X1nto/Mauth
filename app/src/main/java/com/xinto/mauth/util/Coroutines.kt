package com.xinto.mauth.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

inline fun <T> Flow<T>.catchMap(
    crossinline transformation: (Throwable) -> T
): Flow<T> {
    return catch {
        emit(transformation(it))
    }
}