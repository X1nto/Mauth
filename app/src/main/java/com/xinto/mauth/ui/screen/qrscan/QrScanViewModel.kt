package com.xinto.mauth.ui.screen.qrscan

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.domain.OtpRepository

class QrScanViewModel(
    private val repository: OtpRepository
) : ViewModel() {

    fun parseResult(result: com.google.zxing.Result): DomainAccountInfo? {
        return repository.parseUriToAccountInfo(result.text)
    }
}