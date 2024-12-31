package com.xinto.mauth.domain.otp.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DomainOtpRealtimeData {
    val code: String

    @Immutable
    data class Totp(
        override val code: String,
        val progress: Float,
        val countdown: Int,
    ) : DomainOtpRealtimeData

    @Immutable
    data class Hotp(
        override val code: String,
        val count: Int,
    ) : DomainOtpRealtimeData
}