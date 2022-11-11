package com.xinto.mauth.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PreferenceManager {

    fun observePrivateMode(): Flow<Boolean>
    suspend fun setPrivateMode(value: Boolean)

}

class PreferenceManagerImpl(
    context: Context
) : PreferenceManager {

    private val Context.preferences by preferencesDataStore("preferences")
    private val dataStore = context.preferences

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