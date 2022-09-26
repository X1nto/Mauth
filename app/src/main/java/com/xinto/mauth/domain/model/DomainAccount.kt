package com.xinto.mauth.domain.model

import com.xinto.mauth.otp.OtpDigest

interface DomainAccount {
    val id: String
    val secret: String
    val label: String
    val issuer: String
    val algorithm: OtpDigest
    val digits: Int

    data class Totp(
        override val id: String,
        override val secret: String,
        override val label: String,
        override val issuer: String,
        override val algorithm: OtpDigest,
        override val digits: Int,
        val period: Int
    ) : DomainAccount

    data class Hotp(
        override val id: String,
        override val secret: String,
        override val label: String,
        override val issuer: String,
        override val algorithm: OtpDigest,
        override val digits: Int,
        val counter: Int
    ) : DomainAccount

}