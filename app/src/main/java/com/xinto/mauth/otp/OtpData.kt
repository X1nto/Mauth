package com.xinto.mauth.otp

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
