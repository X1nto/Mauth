package com.xinto.mauth.ui.screen.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.backup.BackupRepository
import com.xinto.mauth.util.catchMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class GoogleAuthenticatorExportViewModel(
    private val accounts: List<UUID>,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    val state = flow {
        val uris = backupRepository.exportGoogleAuthenticator(accounts)

        if (uris.isEmpty()) {
            emit(GoogleAuthenticatorExportState.Empty)
        } else {
            emit(GoogleAuthenticatorExportState.Success(uris = uris))
        }
    }
    .catchMap { GoogleAuthenticatorExportState.Error }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoogleAuthenticatorExportState.Loading
    )

}
