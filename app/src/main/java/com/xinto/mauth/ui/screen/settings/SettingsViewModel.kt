package com.xinto.mauth.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository
) : ViewModel() {

    val secureMode = settings.getSecureMode()
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
}