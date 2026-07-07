package com.xinto.mauth.core.settings

import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.FontSetting
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import kotlinx.coroutines.flow.StateFlow

interface Settings {
    fun getSecureMode(): StateFlow<Boolean>
    fun getLockOnResume(): StateFlow<Boolean>
    fun getUseBiometrics(): StateFlow<Boolean>
    fun getSortMode(): StateFlow<SortSetting>
    fun getTheme(): StateFlow<ThemeSetting>
    fun getColor(): StateFlow<ColorSetting>
    fun getFont(): StateFlow<FontSetting>

    suspend fun setSecureMode(value: Boolean)
    suspend fun setLockOnResume(value: Boolean)
    suspend fun setUseBiometrics(value: Boolean)
    suspend fun setSortMode(value: SortSetting)
    suspend fun setTheme(value: ThemeSetting)
    suspend fun setColor(value: ColorSetting)
    suspend fun setFont(value: FontSetting)
}
