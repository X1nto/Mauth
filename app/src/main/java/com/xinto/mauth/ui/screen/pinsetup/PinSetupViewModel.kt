package com.xinto.mauth.ui.screen.pinsetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinSetupViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var initialCode: String? = null

    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()

    private val _code = MutableStateFlow("")
    val code = _code.asStateFlow()

    private val _state = MutableStateFlow<PinSetupScreenState>(PinSetupScreenState.Initial)
    val state = _state.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()

    fun next() {
        if (_code.value.isEmpty()) {
            _error.value = true
            return
        }

        if (state.value is PinSetupScreenState.Confirm) {
            val expected = initialCode ?: return
            if (expected != _code.value) {
                _error.value = true
                return
            }

            val confirmed = _code.value
            initialCode = null
            viewModelScope.launch {
                authRepository.updateCode(confirmed)
                _finished.value = true
            }
            return
        }

        _state.value = PinSetupScreenState.Confirm
        _code.update {
            initialCode = it
            ""
        }
    }

    fun previous() {
        if (state.value is PinSetupScreenState.Initial) {
            _finished.value = true
            return
        }

        clear()
        initialCode = null
        _state.value = PinSetupScreenState.Initial
    }

    fun addNumber(number: Char) {
        val retrying = _error.value
        _error.value = false
        _code.update {
            if (retrying) {
                number.toString()
            } else {
                it + number
            }
        }
    }

    fun deleteLast() {
        if (_error.value) {
            clear()
            return
        }
        _code.update { it.dropLast(1) }
    }

    fun clear() {
        _error.value = false
        _code.value = ""
    }

}
