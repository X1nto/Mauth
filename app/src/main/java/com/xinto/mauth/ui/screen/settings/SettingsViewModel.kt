package com.xinto.mauth.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.settings.model.AccountsLayout
import com.xinto.mauth.domain.settings.model.Font
import com.xinto.mauth.domain.AuthRepository
import com.xinto.mauth.domain.settings.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val secureMode = settings.secureMode
    val lockOnResume = settings.lockOnResume
    val biometrics = settings.useBiometrics
    val font = settings.font
    val theme = settings.theme
    val color = settings.color
    val meshGradientBackground = settings.useMeshGradientBackground
    val accountsLayout = settings.accountsLayout
    val showCodesByDefault = settings.showCodesByDefault

    val pinLock = authRepository.isProtected

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

    fun updateFont(newFont: Font) {
        viewModelScope.launch {
            settings.setFont(newFont)
        }
    }

    fun updateAccountsLayout(newLayout: AccountsLayout) {
        viewModelScope.launch {
            settings.setAccountsLayout(newLayout)
        }
    }

    fun updateShowCodesByDefault(value: Boolean) {
        viewModelScope.launch {
            settings.setShowCodesByDefault(value)
        }
    }

    fun updateUseMeshGradient(value: Boolean) {
        viewModelScope.launch {
            settings.setUseMeshGradientBackground(value)
        }
    }
}