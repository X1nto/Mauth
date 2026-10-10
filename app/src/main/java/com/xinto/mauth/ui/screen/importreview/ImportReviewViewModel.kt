package com.xinto.mauth.ui.screen.importreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccount
import com.xinto.mauth.domain.backup.BackupRepository
import com.xinto.mauth.domain.backup.model.DomainImportCandidate
import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.domain.otp.model.OtpSpec
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ImportReviewViewModel(
    private val candidates: List<DomainImportCandidate>,
    private val backupRepository: BackupRepository,
    accountRepository: AccountRepository,
    otpRepository: OtpRepository
) : ViewModel() {

    val accounts = candidates
        .map { candidate ->
            with(accountRepository) {
                candidate.account.toDomainAccount()
            }
        }
        .toImmutableList()

    val duplicates = candidates
        .filter { it.isDuplicate }
        .map { it.id }
        .toImmutableSet()

    private val _selected = MutableStateFlow(
        candidates.filterNot { it.isDuplicate }.map { it.id }
    )
    val selected = _selected.asStateFlow()

    private val _state = MutableStateFlow<ImportReviewState>(ImportReviewState.Reviewing)
    val state = _state.asStateFlow()

    val realtimeData = otpRepository.getOtpRealtimeData(
        specs = candidates.map { candidate ->
            val account = candidate.account
            OtpSpec(
                id = account.id,
                secret = account.secret,
                digits = account.digits,
                algorithm = account.algorithm,
                type = account.type,
                period = account.period
            )
        },
        counters = candidates
            .filter { it.account.type == OtpType.HOTP }
            .associate { it.id to it.account.counter }
    )

    fun setChecked(id: UUID, checked: Boolean) {
        if (_state.value != ImportReviewState.Reviewing)
            return

        _selected.update {
            when {
                checked && id !in it -> it + id
                !checked -> it - id
                else -> it
            }
        }
    }

    fun commit() {
        if (_state.value == ImportReviewState.Importing || _state.value is ImportReviewState.Imported)
            return

        _state.value = ImportReviewState.Importing
        viewModelScope.launch {
            val chosen = candidates.filter { it.id in _selected.value }
            try {
                backupRepository.commit(chosen)
                _state.value = ImportReviewState.Imported(chosen.size)
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                e.printStackTrace()
                _state.value = ImportReviewState.Failed
            }
        }
    }

    fun dismissFailure() {
        _state.value = ImportReviewState.Reviewing
    }
}
