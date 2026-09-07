package com.xinto.mauth.ui.screen.export

import androidx.compose.runtime.Immutable

@Immutable
enum class FileExportState {
    Idle,
    Working,
    Succeeded,
    Failed
}
