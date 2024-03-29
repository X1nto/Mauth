package com.xinto.mauth.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
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

    override fun getUseBiometrics(): Flow<Boolean> {
        return preferences.data.map {
            it[KEY_USE_BIOMETRICS] ?: false
        }
    }

    override fun getSortMode(): Flow<SortSetting> {
        return preferences.data.map {
            it[KEY_SORT_MODE]?.let { name ->
                SortSetting.valueOf(name)
            } ?: SortSetting.DEFAULT
        }
    }

    override fun getTheme(): Flow<ThemeSetting> {
        return preferences.data.map { preferences ->
            preferences[KEY_THEME]?.let { name ->
                ThemeSetting.entries.find { it.name == name }
            } ?: ThemeSetting.DEFAULT
        }
    }

    override fun getColor(): Flow<ColorSetting> {
        return preferences.data.map { preferences ->
            preferences[KEY_COLOR]?.let { name ->
                ColorSetting.entries.find { it.name == name }
            } ?: ColorSetting.DEFAULT
        }
    }

    override suspend fun setSecureMode(value: Boolean) {
        preferences.edit {
            it[KEY_SECURE_MODE] = value
        }
    }

    override suspend fun setUseBiometrics(value: Boolean) {
        preferences.edit {
            it[KEY_USE_BIOMETRICS] = value
        }
    }

    override suspend fun setSortMode(value: SortSetting) {
        preferences.edit {
            it[KEY_SORT_MODE] = value.name
        }
    }

    override suspend fun setTheme(value: ThemeSetting) {
        preferences.edit {
            it[KEY_THEME] = value.name
        }
    }

    override suspend fun setColor(value: ColorSetting) {
        preferences.edit {
            it[KEY_COLOR] = value.name
        }
    }

    private companion object {
        val KEY_SECURE_MODE = booleanPreferencesKey("private_mode")
        val KEY_USE_BIOMETRICS = booleanPreferencesKey("use_biometrics")
        val KEY_SORT_MODE = stringPreferencesKey("sort_mode")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_COLOR = stringPreferencesKey("color")
    }

}