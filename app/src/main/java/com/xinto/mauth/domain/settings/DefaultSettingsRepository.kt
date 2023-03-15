package com.xinto.mauth.domain.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultSettingsRepository(context: Context) : SettingsRepository {

    private val Context.preferences by preferencesDataStore("preferences")
    private val preferences = context.preferences

    override fun getSecureMode(): Flow<Boolean> {
        return preferences.data.map {
            it[KEY_SECURE_MODE] ?: false
        }
    }

    override suspend fun setSecureMode(value: Boolean) {
        preferences.edit {
            it[KEY_SECURE_MODE] = value
        }
    }

    private companion object {
        val KEY_SECURE_MODE = booleanPreferencesKey("private_mode")
    }

}