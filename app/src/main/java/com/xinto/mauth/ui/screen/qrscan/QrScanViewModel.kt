package com.xinto.mauth.ui.screen.qrscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.otp.model.OtpData
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.OtpRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QrScanViewModel(
    private val otpRepository: OtpRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private var batchId = 0
    private val batches = mutableListOf<OtpData>()
    private var lastScannedIndex = -1

    private val _batchData = MutableStateFlow(BatchData(0, 0))
    val batchData = _batchData.asStateFlow()

    private val _scanError = MutableStateFlow<ScanError?>(null)
    val scanError = _scanError.asStateFlow()

    private val _parseEvent = MutableSharedFlow<DomainAccountInfo?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val parseEvent = _parseEvent.asSharedFlow()

    fun parseResult(result: com.google.zxing.Result) {
        when (val parseResult = otpRepository.parseUri(result.text)) {
            is OtpUriParserResult.Multipart -> {
                if (parseResult.batchSize > 1) {
                    // Don't display errors if the newly scanned QR is the same as previous.
                    // This can happen when the QR gets scanned but the user doesn't move the camera
                    if (parseResult.currentBatch == lastScannedIndex)
                        return

                    if (parseResult.currentBatch != lastScannedIndex + 1) {
                        _scanError.value = ScanError.BatchOrderMismatch
                        return
                    }

                    lastScannedIndex = parseResult.currentBatch

                    if (lastScannedIndex == 0) {
                        batchId = parseResult.batchId
                    } else if (batchId != parseResult.batchId) {
                        _scanError.value = ScanError.BatchIdMismatch
                        return
                    }
                }

                viewModelScope.launch {
                    if (parseResult.currentBatch == parseResult.batchSize - 1) {
                        (batches + parseResult.data).forEach {
                            val accountInfo = with(accountRepository) {
                                it.toAccountInfo()
                            }
                            accountRepository.putAccount(accountInfo)
                        }
                        _parseEvent.emit(null)
                    } else {
                        batches.addAll(parseResult.data)
                    }
                }

                _batchData.value = BatchData(
                    current = parseResult.currentBatch + 1,
                    outOf = parseResult.batchSize
                )
            }
            is OtpUriParserResult.Success -> {
                val accountInfo = with(accountRepository) {
                    parseResult.data.toAccountInfo()
                }
                _parseEvent.tryEmit(accountInfo)
            }
            is OtpUriParserResult.Failure -> {}
        }
    }
}