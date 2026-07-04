package com.xinto.mauth.ui.screen.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import com.xinto.mauth.domain.SettingsRepository
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme = settingsRepository.getTheme()

    val color = settingsRepository.getColor()

    fun updateTheme(newTheme: ThemeSetting) {
        viewModelScope.launch {
            settingsRepository.setTheme(newTheme)
        }
    }

    fun updateColor(newColor: ColorSetting) {
        viewModelScope.launch {
            settingsRepository.setColor(newColor)
        }
    }
}