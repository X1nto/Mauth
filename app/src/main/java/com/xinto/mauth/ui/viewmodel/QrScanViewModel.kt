package com.xinto.mauth.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserResult

class QrScanViewModel(
    private val otpUriParser: OtpUriParser
) : ViewModel() {

    fun parseUri(uri: String): AddAccountParams? {
        return when (val result = otpUriParser.parseUriKey(uri)) {
            is OtpUriParserResult.Success -> {
                AddAccountParams(
                    label = result.data.label,
                    issuer = result.data.issuer,
                    secret = result.data.secret,
                    algorithm = result.data.algorithm,
                    type = result.data.type,
                    digits = result.data.digits,
                    counter = result.data.counter ?: 0,
                    period = result.data.period ?: 30,
                )
            }
            is OtpUriParserResult.Failure -> {
                null
            }
        }
    }

}