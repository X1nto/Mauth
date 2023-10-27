package com.xinto.mauth.core.otp.generator

import com.xinto.mauth.core.otp.model.OtpDigest

interface OtpGenerator {

    fun generateHotp(
        secret: ByteArray,
        counter: Long,
        digits: Int = 6,
        digest: OtpDigest = OtpDigest.Sha1
    ): String

    fun generateTotp(
        secret: ByteArray,
        interval: Long,
        seconds: Long,
        digits: Int = 6,
        digest: OtpDigest = OtpDigest.Sha1
    ): String

}
