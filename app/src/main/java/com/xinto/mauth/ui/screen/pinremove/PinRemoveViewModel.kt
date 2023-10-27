package com.xinto.mauth.ui.screen.pinremove

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class PinRemoveViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PinRemoveScreenState>(PinRemoveScreenState.Stale(""))
    val state = _state.asStateFlow()

    /**
     * @return true if the screen should exit
     */
    fun removePin(): Boolean {
        return state.value.let {
            runBlocking {
                authRepository.validate(it.code).also { valid ->
                    if (valid) {
                        authRepository.removeCode()
                        settingsRepository.setUseBiometrics(false)
                    }
                }
            }.also { valid ->
                if (!valid) {
                    _state.value = PinRemoveScreenState.Error
                }
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
            if (it is PinRemoveScreenState.Stale) {
                PinRemoveScreenState.Stale(it.code.dropLast(1))
            } else it
        }
    }

    fun clear() {
        _state.value = PinRemoveScreenState.Stale("")
    }

}