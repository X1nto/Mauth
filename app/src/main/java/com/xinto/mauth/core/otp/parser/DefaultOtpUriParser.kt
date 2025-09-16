package com.xinto.mauth.core.otp.parser

import android.net.Uri
import com.xinto.mauth.core.otp.model.OtpData
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import androidx.core.net.toUri
import com.google.protobuf.InvalidProtocolBufferException
import com.xinto.mauth.GoogleAuthenticator
import com.xinto.mauth.util.Base64
import org.apache.commons.codec.binary.Base32
import kotlin.io.encoding.ExperimentalEncodingApi

class DefaultOtpUriParser : OtpUriParser {

    @OptIn(ExperimentalEncodingApi::class)
    override fun parseOtpUri(keyUri: String): OtpUriParserResult {
        val uri = keyUri.toUri()
        val protocol = uri.scheme?.lowercase()

        return when (protocol) {
            "otpauth" -> decodeSingle(uri)
            "otpauth-migration" -> decodeMultipart(uri)
            else -> OtpUriParserResult.Failure.ERROR_INVALID_PROTOCOL
        }
    }

    private fun decodeSingle(uri: Uri): OtpUriParserResult {
        val type = when (uri.host?.lowercase()) {
            "hotp" -> OtpType.HOTP
            "totp" -> OtpType.TOTP
            else -> return OtpUriParserResult.Failure.ERROR_INVALID_TYPE
        }

        val label = try {
            uri.pathSegments[0]
        } catch (e: IndexOutOfBoundsException) {
            return OtpUriParserResult.Failure.ERROR_MISSING_LABEL
        }

        val paramSecret = uri.getQueryParameter("secret")
            ?: return OtpUriParserResult.Failure.ERROR_MISSING_SECRET

        val paramIssuer = uri.getQueryParameter("issuer") ?: ""

        val paramAlgorithm = uri.getQueryParameter("algorithm") ?: "SHA1"
        val algorithm = try {
            OtpDigest.valueOf(paramAlgorithm)
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

    private fun decodeMultipart(uri: Uri): OtpUriParserResult {
        val data = uri.getQueryParameter("data")
            ?: return OtpUriParserResult.Failure.ERROR_INVALID_MULTIPART

        val payload = try {
            GoogleAuthenticator.MigrationPayload.parseFrom(Base64.decode(data))
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
            return OtpUriParserResult.Failure.ERROR_INVALID_MULTIPART
        }

        val otpData = payload.otpDataList.map {
            OtpData(
                label = it.name,
                issuer = it.issuer,
                secret = Base32().encodeAsString(it.secret.toByteArray()),
                algorithm = when (it.algorithm) {
                    GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA1 -> OtpDigest.SHA1
                    GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA256 -> OtpDigest.SHA256
                    GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA512 -> OtpDigest.SHA512
                    GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_MD5 -> {
                        // TODO add MD5 support?
                        return OtpUriParserResult.Failure.ERROR_INVALID_ALGORITHM
                    }
                    else -> OtpDigest.SHA1
                },
                type = when (it.type) {
                    GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_HOTP -> OtpType.HOTP
                    GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_TOTP -> OtpType.TOTP
                    else -> return OtpUriParserResult.Failure.ERROR_INVALID_TYPE
                },
                digits = when (it.digits) {
                    GoogleAuthenticator.MigrationPayload.Digits.DIGITS_EIGHT -> 8
                    GoogleAuthenticator.MigrationPayload.Digits.DIGITS_SIX -> 6
                    else -> return OtpUriParserResult.Failure.ERROR_INVALID_DIGITS
                },
                period = 30,
                counter = it.counter.toInt(),
            )
        }

        return OtpUriParserResult.Multipart(
            data = otpData,
            batchSize = payload.batchSize,
            currentBatch = payload.batchIndex,
            batchId = payload.batchId
        )
    }

}