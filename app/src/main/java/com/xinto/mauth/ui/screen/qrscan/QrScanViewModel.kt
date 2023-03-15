package com.xinto.mauth.ui.screen.qrscan

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.usecase.ParseUriToAccountInfoUsecase

class QrScanViewModel(
    private val parseUriToAccountInfo: ParseUriToAccountInfoUsecase
) : ViewModel() {

    fun parseResult(result: com.google.zxing.Result): DomainAccountInfo? {
        return parseUriToAccountInfo(result.text)
    }
}