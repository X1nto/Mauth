package com.xinto.mauth.core.otp

import org.apache.commons.codec.binary.Base32

private val base32 = Base32()

fun String.decodeOtpSecret(): ByteArray {
    val trimmed = trim()
        .replace("-", "")
        .replace(" ", "")
    return base32.decode(trimmed)
}
