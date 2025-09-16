package com.xinto.mauth.util

import java.nio.charset.Charset
import java.util.Base64
import android.util.Base64 as AndroidBase64

object Base64 {

    fun decode(source: String): ByteArray {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Base64.getDecoder().decode(source)
        }

        return AndroidBase64.decode(source, AndroidBase64.DEFAULT)
    }

    fun encode(source: ByteArray): ByteArray {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Base64.getEncoder().encode(source)
        }

        return AndroidBase64.encode(source, AndroidBase64.DEFAULT)
    }

    fun encodeString(
        source: ByteArray,
        charset: Charset = Charsets.UTF_8
    ): String {
        return encode(source).toString(charset)
    }

}