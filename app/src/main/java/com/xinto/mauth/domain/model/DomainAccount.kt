package com.xinto.mauth.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.xinto.mauth.otp.OtpDigest
import java.util.*

@Immutable
sealed class DomainAccount {
    abstract val id: UUID
    abstract val icon: Uri?
    abstract val secret: String
    abstract val label: String
    abstract val issuer: String
    abstract val algorithm: OtpDigest
    abstract val digits: Int

    val shortLabel by lazy {
        label.filter { it.isUpperCase() }.ifEmpty {
            label[0].uppercase()
        }
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
        val counter: Int
    ) : DomainAccount()

}