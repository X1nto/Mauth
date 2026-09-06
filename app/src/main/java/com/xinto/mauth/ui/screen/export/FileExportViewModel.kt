package com.xinto.mauth.ui.screen.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.backup.model.BackupFormat
import com.xinto.mauth.domain.backup.BackupRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FileExportViewModel(
    private val accounts: List<UUID>,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FileExportState.Idle)
    val state = _state.asStateFlow()

    fun export(format: BackupFormat, destination: Uri, password: CharSequence?) {
        viewModelScope.launch {
            _state.value = FileExportState.Working

            val password = password?.let { password ->
                CharArray(password.length) {
                    password[it]
                }
            }

            try {
                backupRepository.export(
                    format = format,
                    password = password,
                    target = destination,
                    accountIds = accounts
                )
                _state.value = FileExportState.Succeeded
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                e.printStackTrace()
                _state.value = FileExportState.Failed
            }
        }
    }

}