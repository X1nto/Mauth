package com.xinto.mauth.otp.parser

import android.net.Uri
import com.xinto.mauth.otp.OtpData
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType

sealed interface OtpUriParserResult {
    class Success(val data: OtpData) : OtpUriParserResult
    class Failure(val error: OtpUriParserError) : OtpUriParserResult
}

enum class OtpUriParserError {
    ERROR_INVALID_PROTOCOL,
    ERROR_INVALID_TYPE,
    ERROR_INVALID_ALGORITHM,
    ERROR_INVALID_DIGITS,
    ERROR_INVALID_PERIOD,
    ERROR_INVALID_COUNTER,
    ERROR_MISSING_LABEL,
    ERROR_MISSING_SECRET,
    ERROR_MISSING_COUNTER,
}

interface OtpUriParser {

    fun parseOtpUri(keyUri: String): OtpUriParserResult

}

class OtpUriParserImpl : OtpUriParser {

    override fun parseOtpUri(keyUri: String): OtpUriParserResult {
        val uri = Uri.parse(keyUri)

        val protocol = uri.scheme
        if (protocol != "otpauth") {
            return OtpUriParserResult.Failure(OtpUriParserError.ERROR_INVALID_PROTOCOL)
        }

        val type = when (uri.host) {
            "hotp" -> OtpType.Hotp
            "totp" -> OtpType.Totp
            else -> return OtpUriParserResult.Failure(OtpUriParserError.ERROR_INVALID_TYPE)
        }

        val label = try {
            uri.pathSegments[0]
        } catch (e: IndexOutOfBoundsException) {
            return OtpUriParserResult.Failure(OtpUriParserError.ERROR_MISSING_LABEL)
        }

        val paramSecret = uri.getQueryParameter("secret")
            ?: return OtpUriParserResult.Failure(OtpUriParserError.ERROR_MISSING_SECRET)

        val paramIssuer = uri.getQueryParameter("issuer") ?: ""

        val paramAlgorithm = uri.getQueryParameter("algorithm") ?: "SHA1"
        val algorithm = getDigestFromUriAlgorithm(paramAlgorithm)
            ?: return OtpUriParserResult.Failure(OtpUriParserError.ERROR_INVALID_ALGORITHM)

        val paramDigits = uri.getQueryParameter("digits") ?: "6"
        val digits = paramDigits.toIntOrNull()
            ?: return OtpUriParserResult.Failure(OtpUriParserError.ERROR_INVALID_DIGITS)

        val paramPeriod = uri.getQueryParameter("period") ?: "30"
        val period = try {
            if (type == OtpType.Hotp) null else paramPeriod.toInt()
        } catch (e: NumberFormatException) {
            return OtpUriParserResult.Failure(OtpUriParserError.ERROR_INVALID_PERIOD)
        }

        val paramCounter = uri.getQueryParameter("counter")
        if (type == OtpType.Hotp && paramCounter == null) {
            return OtpUriParserResult.Failure(OtpUriParserError.ERROR_MISSING_COUNTER)
        }
        val counter = try {
            paramCounter?.toInt()
        } catch (e: NumberFormatException) {
            return OtpUriParserResult.Failure(OtpUriParserError.ERROR_INVALID_COUNTER)
        }

        val otpData = OtpData(
            label = label,
            issuer = paramIssuer,
            secret = paramSecret,
            algorithm = algorithm,
            type = type,
            digits = digits,
            period = period,
            counter = counter,
        )

        return OtpUriParserResult.Success(otpData)
    }

    private fun getDigestFromUriAlgorithm(algorithm: String): OtpDigest? {
        return when (algorithm.uppercase()) {
            "SHA1" -> OtpDigest.Sha1
            "SHA256" -> OtpDigest.Sha256
            "SHA512" -> OtpDigest.Sha512
            else -> null
        }
    }
}