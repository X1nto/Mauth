package com.xinto.mauth.ui.screen.pinsetup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PinSetupViewModel : ViewModel() {

    private var initialCode: String? = null

    private val _code = MutableStateFlow("")
    val code = _code.asStateFlow()

    private val _state = MutableStateFlow<PinSetupScreenState>(PinSetupScreenState.Initial)
    val state = _state.asStateFlow()

    /**
     * @return true if the screen should exit
     */
    fun next(): Boolean {
        if (state.value is PinSetupScreenState.Confirm) {
            return initialCode == code.value
        }

        _state.value = PinSetupScreenState.Confirm
        _code.update {
            initialCode = it
            ""
        }
        return false
    }

    /**
     * @return true if the screen should exit
     */
    fun previous(): Boolean {
        if (state.value is PinSetupScreenState.Initial) {
            return true
        }

        clear()
        _state.value = PinSetupScreenState.Initial
        return false
    }

    fun addNumber(number: Char) {
        _code.update { it + number }
    }

    fun deleteLast() {
        _code.update { it.dropLast(1) }
    }

    fun clear() {
        _code.value = ""
    }

}