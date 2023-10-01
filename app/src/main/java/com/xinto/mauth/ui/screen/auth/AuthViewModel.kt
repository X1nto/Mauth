package com.xinto.mauth.ui.screen.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {

    private val _code = MutableStateFlow("")
    val code = _code.asStateFlow()

    fun insertNumber(number: Char): Boolean {
        return _code.getAndUpdate {
            it + number
        } == "5746"
    }

    fun deleteNumber() {
        _code.update { it.dropLast(1) }
    }

    fun clear() {
        _code.value = ""
    }
}