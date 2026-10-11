package com.xinto.mauth.ui.screen.qrscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.backup.BackupAccount
import com.xinto.mauth.core.backup.GoogleAuthenticatorBackup
import com.xinto.mauth.core.backup.RejectedEntry
import com.xinto.mauth.core.otp.OtpUriParserResult
import com.xinto.mauth.core.otp.parseOtpUri
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.backup.BackupRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QrScanViewModel(
    private val accountRepository: AccountRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private var batchId = 0
    private var batchCount = 0
    private val batches = mutableListOf<BackupAccount>()
    private val rejected = mutableListOf<RejectedEntry>()
    private var lastScannedIndex = -1

    private val _batchData = MutableStateFlow(BatchData(0, 0))
    val batchData = _batchData.asStateFlow()

    private val _scanError = MutableStateFlow<ScanError?>(null)
    val scanError = _scanError.asStateFlow()

    private val _parseEvent = MutableSharedFlow<QrScanEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val parseEvent = _parseEvent.asSharedFlow()

    fun parseResult(result: com.google.zxing.Result) {
        when (val importResult = GoogleAuthenticatorBackup.import(result.text)) {
            is GoogleAuthenticatorBackup.Result.Success -> onBatch(importResult.batch)
            GoogleAuthenticatorBackup.Result.Failure.ERROR_INVALID_PROTOCOL -> onSingle(result.text)
            GoogleAuthenticatorBackup.Result.Failure.ERROR_INVALID_DATA -> {}
        }
    }

    private fun onBatch(batch: GoogleAuthenticatorBackup.Batch) {
        if (lastScannedIndex >= 0 && (batch.id != batchId || batch.count != batchCount)) {
            _scanError.value = ScanError.BatchIdMismatch
            return
        }

        if (batch.index == lastScannedIndex)
            return

        if (batch.index != lastScannedIndex + 1) {
            _scanError.value = ScanError.BatchOrderMismatch
            return
        }

        if (lastScannedIndex == -1) {
            batchId = batch.id
            batchCount = batch.count
        }
        lastScannedIndex = batch.index

        viewModelScope.launch {
            if (batch.index == batch.count - 1) {
                val outcome = backupRepository.import(
                    accounts = batches + batch.accounts,
                    rejected = rejected + batch.rejected
                )
                _parseEvent.emit(QrScanEvent.Batch(outcome))
            } else {
                batches.addAll(batch.accounts)
                rejected.addAll(batch.rejected)
            }
        }

        _batchData.value = BatchData(
            current = batch.index + 1,
            outOf = batch.count
        )
    }

    private fun onSingle(text: String) {
        when (val parseResult = text.parseOtpUri()) {
            is OtpUriParserResult.Success -> {
                val accountInfo = with(accountRepository) {
                    parseResult.data.toAccountInfo()
                }
                _parseEvent.tryEmit(QrScanEvent.Single(accountInfo))
            }
            is OtpUriParserResult.Failure -> {}
        }
    }
}