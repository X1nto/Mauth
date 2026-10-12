package com.xinto.mauth.util

import kotlin.text.iterator

/**
 * RFC 4648 Base32 with lenient decoding and without padding.
 */
object Base32 {

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun encode(bytes: ByteArray): String {
        val result = StringBuilder((bytes.size * 8 + 4) / 5)
        var buffer = 0
        var bits = 0

        for (byte in bytes) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xFF)
            bits += 8
            while (bits >= 5) {
                bits -= 5
                result.append(ALPHABET[(buffer shr bits) and 0x1F])
            }
        }

        if (bits > 0) {
            result.append(ALPHABET[(buffer shl (5 - bits)) and 0x1F])
        }

        return result.toString()
    }

    fun decode(text: CharSequence): ByteArray {
        val result = ByteArray(text.length * 5 / 8)
        var count = 0
        var buffer = 0
        var bits = 0

        for (char in text) {
            if (char == '=')
                break

            val value = valueOf(char)
            if (value < 0)
                continue

            buffer = (buffer shl 5) or value
            bits += 5
            if (bits >= 8) {
                bits -= 8
                result[count++] = (buffer shr bits).toByte()
            }
        }

        return result.copyOf(count)
    }

    private fun valueOf(char: Char): Int {
        return when (char) {
            in 'A'..'Z' -> char - 'A'
            in 'a'..'z' -> char - 'a'
            in '2'..'7' -> char - '2' + 26
            else -> -1
        }
    }
}
