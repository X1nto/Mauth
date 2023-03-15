package com.xinto.mauth.core.otp.transformer

import org.apache.commons.codec.binary.Base32

class DefaultKeyTransformer(
    private val base32: Base32
) : KeyTransformer {

    override fun transformToBytes(key: String): ByteArray {
        val trimmed = key.trim()
            .replace("-", "")
            .replace(" ", "")
        return base32.decode(trimmed)
    }
}