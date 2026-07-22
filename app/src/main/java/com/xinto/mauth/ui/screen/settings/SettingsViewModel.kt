package com.xinto.mauth.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.settings.model.FontSetting
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val secureMode = settings.getSecureMode()
    val lockOnResume = settings.getLockOnResume()
    val biometrics = settings.getUseBiometrics()
    val font = settings.getFont()
    val meshGradientBackground = settings.getUseMeshGradientBackground()

    val pinLock = authRepository.observeIsProtected()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateSecureMode(newSecureMode: Boolean) {
        viewModelScope.launch {
            settings.setSecureMode(newSecureMode)
        }
    }

    fun updateLockOnResume(newLockOnResume: Boolean) {
        viewModelScope.launch {
            settings.setLockOnResume(newLockOnResume)
        }
    }

    fun toggleBiometrics() {
        viewModelScope.launch {
            settings.setUseBiometrics(!biometrics.value)
        }
    }

    fun updateFont(newFont: FontSetting) {
        viewModelScope.launch {
            settings.setFont(newFont)
        }
    }

    fun updateUseMeshGradient(value: Boolean) {
        viewModelScope.launch {
            settings.setUseMeshGradientBackground(value)
        }
    }
}