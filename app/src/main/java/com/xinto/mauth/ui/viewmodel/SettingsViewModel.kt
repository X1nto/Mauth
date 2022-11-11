package com.xinto.mauth.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    var privateMode by mutableStateOf(false)
        private set

    fun updatePrivateMode(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPrivateMode(value)
        }
    }

    init {
        settingsRepository.observePrivateMode()
            .onEach { privateMode = it }
            .launchIn(viewModelScope)
    }

}