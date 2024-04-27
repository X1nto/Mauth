package com.xinto.mauth.ui.screen.export

import com.xinto.mauth.domain.account.model.DomainAccount

sealed interface ExportScreenState {

    data object Loading : ExportScreenState

    data class Success(val accounts: List<DomainAccount>) : ExportScreenState

    data object Error : ExportScreenState

}