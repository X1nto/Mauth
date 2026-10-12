package com.xinto.mauth.core.backup

import androidx.core.net.toUri
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import com.xinto.mauth.GoogleAuthenticator
import com.xinto.mauth.util.Base32
import com.xinto.mauth.core.otp.pairLabelAndIssuer
import kotlin.io.encoding.Base64
import java.net.URLEncoder
import java.util.UUID
import kotlin.random.Random

object GoogleAuthenticatorBackup {

    data class Batch(
        val accounts: List<BackupAccount>,
        val rejected: List<RejectedEntry>,
        val index: Int,
        val count: Int,
        val id: Int
    )

    sealed interface Result {
        data class Success(val batch: Batch) : Result

        enum class Failure : Result {
            ERROR_INVALID_PROTOCOL,
            ERROR_INVALID_DATA
        }
    }

    fun import(content: String): Result {
        val uri = content.toUri()

        if (!uri.isHierarchical)
            return Result.Failure.ERROR_INVALID_PROTOCOL

        if (uri.scheme?.lowercase() != "otpauth-migration")
            return Result.Failure.ERROR_INVALID_PROTOCOL

        val data = uri.getQueryParameter("data")
            ?: return Result.Failure.ERROR_INVALID_DATA

        val bytes = try {
            Base64.Mime.decode(data)
        } catch (e: IllegalArgumentException) {
            return Result.Failure.ERROR_INVALID_DATA
        }

        val payload = try {
            GoogleAuthenticator.MigrationPayload.parseFrom(bytes)
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
            return Result.Failure.ERROR_INVALID_DATA
        }

        val accounts = mutableListOf<BackupAccount>()
        val rejected = mutableListOf<RejectedEntry>()

        for (entry in payload.otpDataList) {
            val (name, issuer) = pairLabelAndIssuer(entry.name, entry.issuer)

            val algorithm = when (entry.algorithm) {
                GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_UNSPECIFIED,
                GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA1 -> BackupOtpAlgorithm.SHA1
                GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA256 -> BackupOtpAlgorithm.SHA256
                GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA512 -> BackupOtpAlgorithm.SHA512
                // TODO add MD5 support?
                GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_MD5,
                GoogleAuthenticator.MigrationPayload.Algorithm.UNRECOGNIZED -> null
            }
            val type = when (entry.type) {
                GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_HOTP -> BackupOtpType.HOTP
                GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_TOTP -> BackupOtpType.TOTP
                GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_UNSPECIFIED,
                GoogleAuthenticator.MigrationPayload.OtpType.UNRECOGNIZED -> null
            }
            val digits = when (entry.digits) {
                GoogleAuthenticator.MigrationPayload.Digits.DIGITS_UNSPECIFIED,
                GoogleAuthenticator.MigrationPayload.Digits.DIGITS_SIX -> 6
                GoogleAuthenticator.MigrationPayload.Digits.DIGITS_EIGHT -> 8
                GoogleAuthenticator.MigrationPayload.Digits.UNRECOGNIZED -> null
            }

            if (algorithm == null || type == null || digits == null) {
                rejected.add(RejectedEntry(name, issuer, ImportFailure.Unrecognized))
                continue
            }

            accounts.add(
                BackupAccount(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    issuer = issuer,
                    secret = Base32.encode(entry.secret.toByteArray()),
                    type = type,
                    algorithm = algorithm,
                    digits = digits,
                    period = if (type == BackupOtpType.TOTP) 30 else null,
                    counter = if (type == BackupOtpType.HOTP) entry.counter.toInt() else null
                )
            )
        }

        return Result.Success(
            Batch(
                accounts = accounts,
                rejected = rejected,
                index = payload.batchIndex,
                count = payload.batchSize,
                id = payload.batchId
            )
        )
    }

    fun export(contents: BackupData): List<String> {
        val protoData = contents.accounts.mapNotNull {
            GoogleAuthenticator.MigrationPayload.OtpData.newBuilder()
                .setIssuer(it.issuer)
                .setName(it.name)
                .setSecret(ByteString.copyFrom(Base32.decode(it.secret)))
                .setAlgorithm(
                    when (it.algorithm) {
                        BackupOtpAlgorithm.SHA1 -> GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA1
                        BackupOtpAlgorithm.SHA256 -> GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA256
                        BackupOtpAlgorithm.SHA512 -> GoogleAuthenticator.MigrationPayload.Algorithm.ALGORITHM_SHA512
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
                        BackupOtpType.HOTP -> GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_HOTP
                        BackupOtpType.TOTP -> GoogleAuthenticator.MigrationPayload.OtpType.OTP_TYPE_TOTP
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
}
