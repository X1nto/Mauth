package com.xinto.mauth.ui.screen.font

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.settings.model.FontSetting
import com.xinto.mauth.domain.SettingsRepository
import kotlinx.coroutines.launch

class FontViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val font = settingsRepository.getFont()

    fun updateFont(newFont: FontSetting) {
        viewModelScope.launch {
            settingsRepository.setFont(newFont)
        }
    }
}