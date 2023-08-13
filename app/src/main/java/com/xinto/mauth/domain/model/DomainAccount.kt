package com.xinto.mauth.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.otp.model.OtpDigest
import java.util.*

val DomainAccount.shortLabel: String
    get() {
        return label.filter {
            it.isUpperCase()
        }.ifEmpty {
            label[0].uppercase()
        }.take(3)
    }

@Immutable
sealed interface DomainAccount {
    val id: UUID
    val icon: Uri?
    val secret: String
    val label: String
    val issuer: String
    val algorithm: OtpDigest
    val digits: Int
    val createdMillis: Long

    @Immutable
    data class Totp(
        override val id: UUID,
        override val icon: Uri?,
        override val secret: String,
        override val label: String,
        override val issuer: String,
        override val algorithm: OtpDigest,
        override val digits: Int,
        override val createdMillis: Long,
        val period: Int
    ) : DomainAccount

    @Immutable
    data class Hotp(
        override val id: UUID,
        override val icon: Uri?,
        override val secret: String,
        override val label: String,
        override val issuer: String,
        override val algorithm: OtpDigest,
        override val digits: Int,
        override val createdMillis: Long,
    ) : DomainAccount

}