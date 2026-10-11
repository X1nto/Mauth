package com.xinto.mauth.ui.screen.qrscan

import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.backup.ImportOutcome

sealed interface QrScanEvent {
    data class Single(val info: DomainAccountInfo) : QrScanEvent

    data class Batch(val data: ImportOutcome.Parsed) : QrScanEvent
}
