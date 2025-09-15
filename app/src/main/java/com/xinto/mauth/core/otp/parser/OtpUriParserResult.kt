package com.xinto.mauth.core.otp.parser

import com.xinto.mauth.core.otp.model.OtpData

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