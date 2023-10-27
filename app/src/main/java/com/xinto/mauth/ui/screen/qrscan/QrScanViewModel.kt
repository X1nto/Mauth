package com.xinto.mauth.ui.screen.qrscan

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.OtpRepository

class QrScanViewModel(
    private val repository: OtpRepository
) : ViewModel() {

    fun parseResult(result: com.google.zxing.Result): DomainAccountInfo? {
        return repository.parseUriToAccountInfo(result.text)
    }
}