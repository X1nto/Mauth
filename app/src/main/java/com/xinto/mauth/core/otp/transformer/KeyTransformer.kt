package com.xinto.mauth.core.otp.transformer

interface KeyTransformer {

    fun transformToBytes(key: String): ByteArray

}

