package com.xinto.mauth.domain.account.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.otp.model.OtpDigest
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

    val shortLabel by lazy {
        label.filter {
            it.isUpperCase()
        }.ifEmpty {
            label[0].uppercase()
        }.take(3)
    }

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