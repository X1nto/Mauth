package com.xinto.mauth.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xinto.mauth.core.settings.model.SortSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultSettings(context: Context) : Settings {

    private val Context.preferences by preferencesDataStore("preferences")
    private val preferences = context.preferences

    override fun getSecureMode(): Flow<Boolean> {
        return preferences.data.map {
            it[KEY_SECURE_MODE] ?: false
        }
    }

    override fun getSortMode(): Flow<SortSetting> {
        return preferences.data.map {
            it[KEY_SORT_MODE]?.let { name ->
                SortSetting.valueOf(name)
            } ?: SortSetting.DEFAULT
        }
    }

    override suspend fun setSecureMode(value: Boolean) {
        preferences.edit {
            it[KEY_SECURE_MODE] = value
        }
    }

    override suspend fun setSortMode(value: SortSetting) {
        preferences.edit {
            it[KEY_SORT_MODE] = value.name
        }
    }

    private companion object {
        val KEY_SECURE_MODE = booleanPreferencesKey("private_mode")
        val KEY_SORT_MODE = stringPreferencesKey("sort_mode")
    }

}