package com.xinto.mauth.otp.transformer

import org.apache.commons.codec.binary.Base32

interface KeyTransformer {

    fun transformToBytes(key: String): ByteArray

}

class KeyTransformerImpl(
    private val base32: Base32
) : KeyTransformer {

    override fun transformToBytes(key: String): ByteArray {
        val trimmed = key.trim().replace("-", "").replace(" ", "")
        return base32.decode(trimmed)
    }

}