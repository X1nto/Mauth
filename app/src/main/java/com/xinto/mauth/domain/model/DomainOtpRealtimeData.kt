package com.xinto.mauth.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DomainOtpRealtimeData {
    val code: String

    data class Totp(
        override val code: String,
        val progress: Float,
        val countdown: Int,
    ) : DomainOtpRealtimeData

    data class Hotp(
        override val code: String,
        val count: Int,
    ) : DomainOtpRealtimeData
}