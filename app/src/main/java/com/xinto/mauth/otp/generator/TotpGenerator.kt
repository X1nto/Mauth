package com.xinto.mauth.otp.generator

import com.xinto.mauth.otp.OtpDigest
import kotlin.math.floor

interface TotpGenerator {

    fun generate(
        secret: ByteArray,
        interval: Long,
        seconds: Long,
        digits: Int = 6,
        digest: OtpDigest = OtpDigest.Sha1
    ): String

}

class TotpGeneratorImpl(private val hotpGenerator: HotpGenerator) : TotpGenerator {

    override fun generate(
        secret: ByteArray,
        interval: Long,
        seconds: Long,
        digits: Int,
        digest: OtpDigest
    ): String {
        val counter = floor((seconds / interval).toDouble()).toLong()
        return hotpGenerator.generate(secret, counter, digits, digest)
    }

}