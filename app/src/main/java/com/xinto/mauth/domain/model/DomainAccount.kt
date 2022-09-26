package com.xinto.mauth.domain.model

import android.net.Uri
import com.xinto.mauth.otp.OtpDigest

sealed class DomainAccount {
    abstract val id: String
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

    data class Totp(
        override val id: String,
        override val icon: Uri?,
        override val secret: String,
        override val label: String,
        override val issuer: String,
        override val algorithm: OtpDigest,
        override val digits: Int,
        val period: Int
    ) : DomainAccount()

    data class Hotp(
        override val id: String,
        override val icon: Uri?,
        override val secret: String,
        override val label: String,
        override val issuer: String,
        override val algorithm: OtpDigest,
        override val digits: Int,
        val counter: Int
    ) : DomainAccount()

}