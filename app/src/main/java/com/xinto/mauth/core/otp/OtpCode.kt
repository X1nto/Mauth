package com.xinto.mauth.core.otp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.math.pow

fun generateTotp(
    secret: ByteArray,
    interval: Long,
    seconds: Long,
    digits: Int = 6,
    digest: OtpDigest = OtpDigest.SHA1
): String {
    val counter = floor((seconds / interval).toDouble()).toLong()
    return generateHotp(secret, counter, digits, digest)
}

fun generateHotp(
    secret: ByteArray,
    counter: Long,
    digits: Int = 6,
    digest: OtpDigest = OtpDigest.SHA1
): String {
    val mac = Mac.getInstance(digest.algorithmName).apply {
        init(SecretKeySpec(secret, "RAW"))
    }

    val byteCounter = ByteBuffer.allocate(8)
        .putLong(counter)
        .array()
    val hash = mac.doFinal(byteCounter)

    val offset = hash[hash.size - 1].toInt() and 0xF
    val code = ((hash[offset].toInt() and 0x7f) shl 24) or
            ((hash[offset + 1].toInt() and 0xff) shl 16) or
            ((hash[offset + 2].toInt() and 0xff) shl 8) or
            ((hash[offset + 3].toInt() and 0xff))

    val truncatedCode = (code % 10.0.pow(digits.toDouble())).toInt()

    return truncatedCode.toString().padStart(digits, '0')
}

private val OtpDigest.algorithmName: String
    get() {
        return when (this) {
            OtpDigest.SHA1 -> "HmacSHA1"
            OtpDigest.SHA256 -> "HmacSHA256"
            OtpDigest.SHA512 -> "HmacSHA512"
        }
    }
