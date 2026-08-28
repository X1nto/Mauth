package com.xinto.mauth.ui.screen.pinremove

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinRemoveViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PinRemoveScreenState>(PinRemoveScreenState.Stale(""))
    val state = _state.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()

    fun removePin() {
        val current = _state.value
        if (current !is PinRemoveScreenState.Stale) {
            return
        }

        viewModelScope.launch {
            if (authRepository.validate(current.code)) {
                settingsRepository.setUseBiometrics(false)
                authRepository.removeCode()
                _finished.value = true
            } else {
                _state.value = PinRemoveScreenState.Error(current.code)
            }
        }
    }

    fun addNumber(number: Char) {
        _state.update {
            when (it) {
                is PinRemoveScreenState.Stale -> PinRemoveScreenState.Stale(it.code + number)
                is PinRemoveScreenState.Error -> PinRemoveScreenState.Stale(number.toString())
            }
        }
    }

    fun deleteLast() {
        _state.update {
            when (it) {
                is PinRemoveScreenState.Stale -> PinRemoveScreenState.Stale(it.code.dropLast(1))
                is PinRemoveScreenState.Error -> PinRemoveScreenState.Stale("")
            }
        }
    }

    fun clear() {
        _state.value = PinRemoveScreenState.Stale("")
    }

}