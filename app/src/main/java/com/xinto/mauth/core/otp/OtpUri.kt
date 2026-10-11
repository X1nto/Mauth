package com.xinto.mauth.core.otp

import android.net.Uri
import androidx.core.net.toUri

sealed interface OtpUriParserResult {
    data class Success(val data: OtpData) : OtpUriParserResult

    enum class Failure : OtpUriParserResult {
        ERROR_INVALID_PROTOCOL,
        ERROR_INVALID_TYPE,
        ERROR_INVALID_ALGORITHM,
        ERROR_INVALID_DIGITS,
        ERROR_INVALID_PERIOD,
        ERROR_INVALID_COUNTER,
        ERROR_MISSING_LABEL,
        ERROR_MISSING_SECRET,
        ERROR_MISSING_COUNTER
    }
}

fun String.parseOtpUri(): OtpUriParserResult {
    val uri = this.toUri()

    if (!uri.isHierarchical)
        return OtpUriParserResult.Failure.ERROR_INVALID_PROTOCOL

    if (uri.scheme?.lowercase() != "otpauth")
        return OtpUriParserResult.Failure.ERROR_INVALID_PROTOCOL

    val type = try {
        OtpType.valueOf(uri.host.orEmpty().uppercase())
    } catch (e: IllegalArgumentException) {
        return OtpUriParserResult.Failure.ERROR_INVALID_TYPE
    }

    val rawLabel = try {
        uri.pathSegments[0]
    } catch (e: IndexOutOfBoundsException) {
        return OtpUriParserResult.Failure.ERROR_MISSING_LABEL
    }

    val paramSecret = uri.getQueryParameter("secret")
        ?: return OtpUriParserResult.Failure.ERROR_MISSING_SECRET

    val (label, paramIssuer) = pairLabelAndIssuer(rawLabel, uri.getQueryParameter("issuer"))

    val paramAlgorithm = uri.getQueryParameter("algorithm") ?: "SHA1"
    val algorithm = try {
        OtpDigest.valueOf(paramAlgorithm.uppercase())
    } catch (e: IllegalArgumentException) {
        return OtpUriParserResult.Failure.ERROR_INVALID_ALGORITHM
    }

    val paramDigits = uri.getQueryParameter("digits") ?: "6"
    val digits = paramDigits.toIntOrNull()
        ?: return OtpUriParserResult.Failure.ERROR_INVALID_DIGITS

    val paramPeriod = uri.getQueryParameter("period") ?: "30"
    val period = try {
        if (type == OtpType.HOTP) null else paramPeriod.toInt()
    } catch (e: NumberFormatException) {
        return OtpUriParserResult.Failure.ERROR_INVALID_PERIOD
    }

    val paramCounter = uri.getQueryParameter("counter")
    if (type == OtpType.HOTP && paramCounter == null) {
        return OtpUriParserResult.Failure.ERROR_MISSING_COUNTER
    }

    val counter = try {
        paramCounter?.toInt()
    } catch (e: NumberFormatException) {
        return OtpUriParserResult.Failure.ERROR_INVALID_COUNTER
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

fun pairLabelAndIssuer(rawLabel: String, paramIssuer: String?): Pair<String, String> {
    val issuer = paramIssuer.orEmpty()
    val account = rawLabel.substringAfter(':', missingDelimiterValue = "").trimStart()
    if (account.isEmpty())
        return rawLabel to issuer

    return account to issuer.ifEmpty { rawLabel.substringBefore(':').trim() }
}

fun OtpData.toOtpUri(): String {
    val uriBuilder = Uri.Builder()
        .scheme("otpauth")
        .appendPath(if (issuer.isNotBlank()) "$issuer:$label" else label)
        .appendQueryParameter("secret", secret)
        .appendQueryParameter("algorithm", algorithm.name)
        .appendQueryParameter("digits", digits.toString())

    if (issuer.isNotBlank()) {
        uriBuilder.appendQueryParameter("issuer", issuer)
    }

    val uri = when (type) {
        OtpType.TOTP -> {
            uriBuilder
                .authority("totp")
                .appendQueryParameter("period", (period ?: 30).toString())
        }
        OtpType.HOTP -> {
            uriBuilder
                .authority("hotp")
                .appendQueryParameter("counter", (counter ?: 0).toString())
        }
    }
    return uri.toString()
}
