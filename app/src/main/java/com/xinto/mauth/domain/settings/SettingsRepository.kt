package com.xinto.mauth.domain.settings

import com.xinto.mauth.domain.settings.model.SortSetting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getSecureMode(): Flow<Boolean>
    fun getSortMode(): Flow<SortSetting>

    suspend fun setSecureMode(value: Boolean)
    suspend fun setSortMode(value: SortSetting)

}