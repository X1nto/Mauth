package com.xinto.mauth.ui.screen.auth

import androidx.lifecycle.ViewModel
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _code = MutableStateFlow("")
    val code = _code.asStateFlow()

    val useBiometrics = settingsRepository.getUseBiometrics()
    val useMeshGradientBackground = settingsRepository.getUseMeshGradientBackground()

    fun insertNumber(number: Char) {
        _code.update { it + number }
    }

    fun deleteNumber() {
        _code.update { it.dropLast(1) }
    }

    fun clear() {
        _code.value = ""
    }

    suspend fun validate(code: String): Boolean {
        return authRepository.validate(code)
    }
}