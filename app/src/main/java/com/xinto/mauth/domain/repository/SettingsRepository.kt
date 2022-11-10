package com.xinto.mauth.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {

    fun observePrivateMode(): Flow<Boolean>
    suspend fun setPrivateMode(value: Boolean)

}

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun observePrivateMode(): Flow<Boolean> {
        return dataStore.data.map {
            it[KEY_PRIVATE_MODE] ?: false
        }
    }

    override suspend fun setPrivateMode(value: Boolean) {
        dataStore.edit {
            it[KEY_PRIVATE_MODE] = value
        }
    }

    private companion object {
        val KEY_PRIVATE_MODE = booleanPreferencesKey("private_mode")
    }

}