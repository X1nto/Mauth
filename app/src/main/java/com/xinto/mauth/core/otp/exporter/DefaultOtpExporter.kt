package com.xinto.mauth.core.otp.exporter

import android.net.Uri
import com.google.protobuf.ByteString
import com.xinto.mauth.GoogleAuthenticator
import com.xinto.mauth.core.otp.model.OtpData
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.util.Base64
import org.apache.commons.codec.binary.Base32
import java.net.URLEncoder
import kotlin.random.Random

class DefaultOtpExporter : OtpExporter {

    override fun exportOtp(data: OtpData): String {
        val uriBuilder = Uri.Builder()
            .scheme("otpauth")
            .appendPath(data.label)
            .appendQueryParameter("secret", data.secret)
            .appendQueryParameter("algorithm", data.algorithm.name)
            .appendQueryParameter("digits", data.digits.toString())

        if (data.issuer.isNotBlank()) {
            uriBuilder.appendQueryParameter("issuer", data.issuer)
        }

        return when (data.type) {
            OtpType.TOTP -> {
                uriBuilder
                    .authority("totp")
                    .appendQueryParameter("period", data.period.toString())
            }
            OtpType.HOTP -> {
                uriBuilder
                    .authority("hotp")
                    .appendQueryParameter("counter", data.period.toString())
            }
        }.toString().also(::println)
    }

    override fun exportBatch(data: List<OtpData>): List<String> {
        val protoData = data.mapNotNull {
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
        protoData.forEachIndexed { i, otpData ->
            if (counter > 300 || i == protoData.lastIndex) {
                migrationBuilders.add(migrationBuilder)
                migrationBuilder = GoogleAuthenticator.MigrationPayload.newBuilder()
                counter = 0
            }

            counter += otpData.serializedSize
            migrationBuilder.addOtpData(otpData)
        }

        val randomBatchId = Random.nextInt()

        val batches = migrationBuilders.mapIndexed { i, builder ->
            builder
                .setBatchIndex(i)
                .setBatchSize(migrationBuilders.size)
                .setBatchId(if (migrationBuilders.size == 1) 0 else randomBatchId)
                .build()
        }

        val data = batches.map {
            buildString {
                append("otpauth-migration://offline?data=")

                val migration = Base64.encodeString(it.toByteArray())
                append(URLEncoder.encode(migration, "UTF-8"))
            }
        }

        return data
    }

}