package com.xinto.mauth.domain.repository

import com.xinto.mauth.preferences.PreferenceManager
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun observePrivateMode(): Flow<Boolean>
    suspend fun setPrivateMode(value: Boolean)

}

class SettingsRepositoryImpl(
    private val preferenceManager: PreferenceManager
) : SettingsRepository {

    override fun observePrivateMode(): Flow<Boolean> {
        return preferenceManager.observePrivateMode()
    }

    override suspend fun setPrivateMode(value: Boolean) {
        preferenceManager.setPrivateMode(value)
    }

}