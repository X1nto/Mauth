package com.xinto.mauth.core.otp.generator

import com.xinto.mauth.core.otp.model.OtpDigest
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.math.pow

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
