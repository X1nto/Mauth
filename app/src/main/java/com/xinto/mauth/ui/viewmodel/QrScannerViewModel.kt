package com.xinto.mauth.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserResult

class QrScannerViewModel(
    private val otpUriParser: OtpUriParser
) : ViewModel() {

    fun acceptSuccessScan(result: com.google.zxing.Result): DomainAccountInfo? {
        return when (val parseResult = otpUriParser.parseOtpUri(result.text)) {
            is OtpUriParserResult.Success -> {
                DomainAccountInfo(
                    id = null,
                    icon = null,
                    label = parseResult.data.label,
                    issuer = parseResult.data.issuer,
                    secret = parseResult.data.secret,
                    algorithm = parseResult.data.algorithm,
                    type = parseResult.data.type,
                    digits = parseResult.data.digits,
                    counter = parseResult.data.counter ?: 0,
                    period = parseResult.data.period ?: 30,
                )
            }
            is OtpUriParserResult.Failure -> null
        }
    }
}