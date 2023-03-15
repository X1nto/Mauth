package com.xinto.mauth.core.otp.model

data class OtpData(
    val label: String,
    val issuer: String,
    val secret: String,
    val algorithm: OtpDigest,
    val type: OtpType,
    val digits: Int,
    val counter: Int?,
    val period: Int?,
)
