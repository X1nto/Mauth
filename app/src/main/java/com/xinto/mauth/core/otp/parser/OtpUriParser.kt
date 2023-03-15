package com.xinto.mauth.core.otp.parser

interface OtpUriParser {
    fun parseOtpUri(keyUri: String): OtpUriParserResult
}