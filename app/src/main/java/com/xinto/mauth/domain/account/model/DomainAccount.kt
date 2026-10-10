package com.xinto.mauth.domain.account.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.otp.OtpDigest
import java.util.UUID

@Immutable
sealed class DomainAccount {
    abstract val id: UUID
    abstract val icon: Uri?
    abstract val secret: String
    abstract val label: String
    abstract val issuer: String
    abstract val algorithm: OtpDigest
    abstract val digits: Int
    abstract val createdMillis: Long

    val shortLabel: String
        get() = monogramOf(issuer, label)

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
    ) : DomainAccount()

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
    ) : DomainAccount()

}

fun monogramOf(issuer: String, label: String): String {
    val source = issuer.ifEmpty { label }
    if (source.isEmpty())
        return ""

    return source
        .filter { it.isUpperCase() }
        .ifEmpty { source[0].uppercase() }
        .take(3)
}