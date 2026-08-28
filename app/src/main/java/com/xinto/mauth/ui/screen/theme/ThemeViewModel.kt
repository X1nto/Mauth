package com.xinto.mauth.ui.screen.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.settings.model.ColorScheme
import com.xinto.mauth.domain.settings.model.Theme
import com.xinto.mauth.domain.settings.SettingsRepository
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme = settingsRepository.theme

    val color = settingsRepository.color

    fun updateTheme(newTheme: Theme) {
        viewModelScope.launch {
            settingsRepository.setTheme(newTheme)
        }
    }

    fun updateColor(newColor: ColorScheme) {
        viewModelScope.launch {
            settingsRepository.setColor(newColor)
        }
    }
}