package com.xinto.mauth.ui.screen.qrscan

import androidx.annotation.StringRes
import com.xinto.mauth.R

enum class ScanError(@param:StringRes val stringRes: Int) {
    BatchIdMismatch(R.string.qrscan_error_batch),
    BatchOrderMismatch(R.string.qrscan_error_order),
}