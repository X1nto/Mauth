package com.xinto.mauth

import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.parser.DefaultOtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class OtpParseTest(
    private val name: String,
    private val uri: String,
) {

    private val parser = DefaultOtpUriParser()

    @Test
    fun parsesIssuerAndAccount() {
        val parseResult = parser.parseOtpUri(uri)
        Assert.assertTrue(parseResult.toString(), parseResult is OtpUriParserResult.Success)

        val data = (parseResult as OtpUriParserResult.Success).data
        Assert.assertEquals("account", data.label)
        Assert.assertEquals("secret", data.secret)
        Assert.assertEquals("issuer", data.issuer)
        Assert.assertEquals(OtpDigest.SHA1, data.algorithm)
        Assert.assertEquals(6, data.digits)
        Assert.assertEquals(30, data.period)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
            arrayOf("issuer in params", "otpauth://totp/account?secret=secret&issuer=issuer&algorithm=sha1&digits=6&period=30"),
            arrayOf("issuer in segment", "otpauth://totp/issuer:account?secret=secret&algorithm=sha1&digits=6&period=30"),
            arrayOf("issuer in both", "otpauth://totp/issuer:account?secret=secret&issuer=issuer&algorithm=sha1&digits=6&period=30"),
        )
    }
}