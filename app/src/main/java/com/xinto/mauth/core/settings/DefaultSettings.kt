package com.xinto.mauth.core.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.xinto.mauth.core.settings.model.ColorSetting
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.core.settings.model.ThemeSetting
import kotlin.enums.enumEntries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DefaultSettings(context: Context) : Settings {

    private val preferences = PreferenceDataStoreFactory.create {
        context.applicationContext.preferencesDataStoreFile("preferences")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val secureMode = preferenceStateFlow { it[KEY_SECURE_MODE] ?: false }
    private val lockOnResume = preferenceStateFlow { it[KEY_LOCK_ON_RESUME] ?: false }
    private val useBiometrics = preferenceStateFlow { it[KEY_USE_BIOMETRICS] ?: false }
    private val sortMode = preferenceStateFlow { it[KEY_SORT_MODE].toEnumOr(SortSetting.DEFAULT) }
    private val theme = preferenceStateFlow { it[KEY_THEME].toEnumOr(ThemeSetting.DEFAULT) }
    private val color = preferenceStateFlow { it[KEY_COLOR].toEnumOr(ColorSetting.DEFAULT) }

    override fun getSecureMode(): StateFlow<Boolean> = secureMode
    override fun getLockOnResume(): StateFlow<Boolean> = lockOnResume
    override fun getUseBiometrics(): StateFlow<Boolean> = useBiometrics
    override fun getSortMode(): StateFlow<SortSetting> = sortMode
    override fun getTheme(): StateFlow<ThemeSetting> = theme
    override fun getColor(): StateFlow<ColorSetting> = color

    override suspend fun setSecureMode(value: Boolean) {
        preferences.edit {
            it[KEY_SECURE_MODE] = value
        }
    }

    override suspend fun setLockOnResume(value: Boolean) {
        preferences.edit {
            it[KEY_LOCK_ON_RESUME] = value
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

    private inline fun <T> preferenceStateFlow(crossinline transform: (Preferences) -> T): StateFlow<T> {
        return preferences.data
            .map(transform)
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = transform(emptyPreferences())
            )
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T {
        return this?.let { name -> enumEntries<T>().find { it.name == name } } ?: default
    }

    private companion object {
        val KEY_SECURE_MODE = booleanPreferencesKey("private_mode")
        val KEY_LOCK_ON_RESUME = booleanPreferencesKey("lock_on_resume")
        val KEY_USE_BIOMETRICS = booleanPreferencesKey("use_biometrics")
        val KEY_SORT_MODE = stringPreferencesKey("sort_mode")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_COLOR = stringPreferencesKey("color")
    }

}