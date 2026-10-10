package com.xinto.mauth.core.otp

import android.net.Uri
import androidx.core.net.toUri
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import com.xinto.mauth.GoogleAuthenticator
import kotlin.io.encoding.Base64
import org.apache.commons.codec.binary.Base32
import java.net.URLEncoder
import kotlin.random.Random

sealed interface OtpUriParserResult {
    data class Success(val data: OtpData) : OtpUriParserResult

    data class Multipart(
        val data: List<OtpData>,
        val currentBatch: Int,
        val batchSize: Int,
        val batchId: Int
    ) : OtpUriParserResult

    enum class Failure : OtpUriParserResult {
        ERROR_INVALID_PROTOCOL,
        ERROR_INVALID_TYPE,
        ERROR_INVALID_ALGORITHM,
        ERROR_INVALID_DIGITS,
        ERROR_INVALID_PERIOD,
        ERROR_INVALID_COUNTER,
        ERROR_MISSING_LABEL,
        ERROR_MISSING_SECRET,
        ERROR_MISSING_COUNTER,
        ERROR_INVALID_MULTIPART
    }
}

fun String.parseOtpUri(): OtpUriParserResult {
    val uri = this.toUri()
    if (!uri.isHierarchical)
        return OtpUriParserResult.Failure.ERROR_INVALID_PROTOCOL

    val protocol = uri.scheme?.lowercase()

    return when (protocol) {
        "otpauth" -> decodeSingle(uri)
        "otpauth-migration" -> decodeMultipart(uri)
        else -> OtpUriParserResult.Failure.ERROR_INVALID_PROTOCOL
    }
}

private fun decodeSingle(uri: Uri): OtpUriParserResult {
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

private fun pairLabelAndIssuer(rawLabel: String, paramIssuer: String?): Pair<String, String> {
    val issuer = paramIssuer.orEmpty()
    val account = rawLabel.substringAfter(':', missingDelimiterValue = "").trimStart()
    if (account.isEmpty())
        return rawLabel to issuer

    return account to issuer.ifEmpty { rawLabel.substringBefore(':').trim() }
}

private fun decodeMultipart(uri: Uri): OtpUriParserResult {
    val data = uri.getQueryParameter("data")
        ?: return OtpUriParserResult.Failure.ERROR_INVALID_MULTIPART

    val bytes = try {
        Base64.Mime.decode(data)
    } catch (e: IllegalArgumentException) {
        return OtpUriParserResult.Failure.ERROR_INVALID_MULTIPART
    }

    val payload = try {
        GoogleAuthenticator.MigrationPayload.parseFrom(bytes)
    } catch (e: InvalidProtocolBufferException) {
        e.printStackTrace()
        return OtpUriParserResult.Failure.ERROR_INVALID_MULTIPART
    }

    val otpData = payload.otpDataList.map {
        val (label, issuer) = pairLabelAndIssuer(it.name, it.issuer)
        OtpData(
            label = label,
            issuer = issuer,
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

fun OtpData.toOtpUri(): String {
    val uriBuilder = Uri.Builder()
        .scheme("otpauth")
        .appendPath(label)
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

fun List<OtpData>.toMigrationUris(): List<String> {
    val protoData = mapNotNull {
        GoogleAuthenticator.MigrationPayload.OtpData.newBuilder()
            .setIssuer(it.issuer)
            .setName(it.label)
            .setSecret(ByteString.copyFrom(Base32().decode(it.secret)))
            .setAlgorithm(
                when (it.algorithm) {
                    OtpDigest.SHA1 -> GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA1
                    OtpDigest.SHA256 -> GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA256
                    OtpDigest.SHA512 -> GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA512
                }
            )
            .setDigits(
                when (it.digits) {
                    6 -> GoogleAuthenticator.MigrationPayload.Digits.DIGITS_SIX
                    8 -> GoogleAuthenticator.MigrationPayload.Digits.DIGITS_EIGHT
                    else -> return@mapNotNull null
                }
            )
            .setType(
                when (it.type) {
                    OtpType.HOTP -> GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_HOTP
                    OtpType.TOTP -> GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_TOTP
                }
            )
            .setCounter(it.counter?.toLong() ?: 0L)
            .build()
    }

    val migrationBuilders = mutableListOf<GoogleAuthenticator.MigrationPayload.Builder>()
    var migrationBuilder = GoogleAuthenticator.MigrationPayload
        .newBuilder()

    var counter = 0
    protoData.forEach { otpData ->
        if (counter > 0 && counter + otpData.serializedSize > 300) {
            migrationBuilders.add(migrationBuilder)
            migrationBuilder = GoogleAuthenticator.MigrationPayload.newBuilder()
            counter = 0
        }

        counter += otpData.serializedSize
        migrationBuilder.addOtpData(otpData)
    }

    if (counter > 0) {
        migrationBuilders.add(migrationBuilder)
    }

    val randomBatchId = Random.nextInt()

    val batches = migrationBuilders.mapIndexed { i, builder ->
        builder
            .setVersion(1)
            .setBatchIndex(i)
            .setBatchSize(migrationBuilders.size)
            .setBatchId(if (migrationBuilders.size == 1) 0 else randomBatchId)
            .build()
    }

    val data = batches.map {
        buildString {
            append("otpauth-migration://offline?data=")

            val migration = Base64.encode(it.toByteArray())
            append(URLEncoder.encode(migration, "UTF-8"))
        }
    }

    return data
}
