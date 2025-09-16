package com.xinto.mauth.ui.screen.export

sealed interface BatchExportState {

    data object Loading : BatchExportState

    data class Success(val data: List<String>): BatchExportState

    data object Error: BatchExportState

}