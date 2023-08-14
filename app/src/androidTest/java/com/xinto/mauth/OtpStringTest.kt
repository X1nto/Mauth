package com.xinto.mauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.parser.DefaultOtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtpStringTest {

    private val parser = DefaultOtpUriParser()
    private val testString = "otpauth://totp/account?secret=secret&issuer=issuer&algorithm=sha1&digits=6&period=30"

    @Test
    fun testStringValidation() {
        val parseResult = parser.parseOtpUri(testString)
        Assert.assertTrue(parseResult.toString(), parseResult is OtpUriParserResult.Success)

        val castResult = parseResult as OtpUriParserResult.Success
        Assert.assertTrue(castResult.data.label == "account")
        Assert.assertTrue(castResult.data.secret == "secret")
        Assert.assertTrue(castResult.data.issuer == "issuer")
        Assert.assertTrue(castResult.data.algorithm == OtpDigest.Sha1)
        Assert.assertTrue(castResult.data.digits ==6)
        Assert.assertTrue(castResult.data.period ==30)
    }

}