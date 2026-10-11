package com.xinto.mauth.ui.screen.importer

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.backup.ImportFailure
import com.xinto.mauth.core.backup.BackupFormat
import com.xinto.mauth.domain.backup.BackupRepository
import com.xinto.mauth.domain.backup.ImportOutcome
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _attempt = MutableStateFlow<ImportAttempt?>(null)
    val attempt = _attempt.asStateFlow()

    private val _parsed = MutableSharedFlow<ImportOutcome.Parsed>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val parsed = _parsed.asSharedFlow()

    private var pending: Uri? = null
    private var pendingSource: BackupFormat? = null

    fun read(uri: Uri, source: BackupFormat, password: CharArray? = null) {
        viewModelScope.launch {
            _attempt.value = ImportAttempt.Working
            when (val outcome = backupRepository.import(uri, source, password)) {
                is ImportOutcome.NeedsPassword -> {
                    pending = uri
                    pendingSource = source
                    _attempt.value = ImportAttempt.AwaitingPassword(
                        source = outcome.source,
                        wrongPassword = false
                    )
                }
                is ImportOutcome.WrongFormat -> {
                    pending = uri
                    pendingSource = source
                    _attempt.value = ImportAttempt.WrongFormat(
                        expected = outcome.expected,
                        actual = outcome.actual
                    )
                }
                is ImportOutcome.Failed -> {
                    if (outcome.reason == ImportFailure.WrongPassword && pending != null) {
                        _attempt.value = ImportAttempt.AwaitingPassword(
                            source = outcome.source,
                            wrongPassword = true
                        )
                    } else {
                        clearPending()
                        _attempt.value = ImportAttempt.Failed(outcome.reason)
                    }
                }
                is ImportOutcome.Parsed -> {
                    clearPending()
                    if (outcome.candidates.isEmpty()) {
                        _attempt.value = ImportAttempt.NothingToImport(outcome.rejected.size)
                    } else {
                        _attempt.value = null
                        _parsed.tryEmit(outcome)
                    }
                }
            }
        }
    }

    fun submitPassword(password: CharArray) {
        val uri = pending ?: return
        val source = pendingSource ?: return
        read(uri, source, password)
    }

    fun retryAs(source: BackupFormat) {
        val uri = pending ?: return
        read(uri, source)
    }

    fun dismiss() {
        clearPending()
        _attempt.value = null
    }

    private fun clearPending() {
        pending = null
        pendingSource = null
    }
}
