package com.xinto.mauth.otp

import com.xinto.mauth.util.Base32
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Base32Test {

    // RFC 4648 section 10, without the padding
    private val vectors = listOf(
        "" to "",
        "f" to "MY",
        "fo" to "MZXQ",
        "foo" to "MZXW6",
        "foob" to "MZXW6YQ",
        "fooba" to "MZXW6YTB",
        "foobar" to "MZXW6YTBOI"
    )

    @Test
    fun `encoding matches the rfc without padding`() {
        for ((plain, encoded) in vectors) {
            assertEquals(encoded, Base32.encode(plain.toByteArray()))
        }
    }

    @Test
    fun `decoding matches the rfc`() {
        for ((plain, encoded) in vectors) {
            assertArrayEquals(plain.toByteArray(), Base32.decode(encoded))
        }
    }

    @Test
    fun `padded input decodes`() {
        assertArrayEquals("foob".toByteArray(), Base32.decode("MZXW6YQ="))
        assertArrayEquals("f".toByteArray(), Base32.decode("MY======"))
    }

    @Test
    fun `lowercase input decodes`() {
        assertArrayEquals("foobar".toByteArray(), Base32.decode("mzxw6ytboi"))
    }

    @Test
    fun `characters outside of alphabet are skipped`() {
        assertArrayEquals("foobar".toByteArray(), Base32.decode(" MZXW-6YTB OI\n"))
    }

    @Test
    fun `padding ends the input`() {
        assertArrayEquals("f".toByteArray(), Base32.decode("MY=ZXW6"))
    }

    @Test
    fun `single character decodes to nothing`() {
        assertEquals(0, Base32.decode("M").size)
    }
}
