package com.xinto.mauth.core.otp.parser

import com.xinto.mauth.core.otp.model.OtpData

sealed interface OtpUriParserResult {
    data class Success(val data: OtpData) : OtpUriParserResult
    data class Failure(val error: OtpUriParserError) : OtpUriParserResult
}