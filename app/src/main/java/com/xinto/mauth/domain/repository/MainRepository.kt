package com.xinto.mauth.domain.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MainRepository {

    fun observeSecureMode(): Flow<Boolean>

}

class MainRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : MainRepository {

    override fun observeSecureMode(): Flow<Boolean> {
        return dataStore.data.map {
            it[KEY_PRIVATE_MODE] ?: false
        }
    }

    private companion object {
        val KEY_PRIVATE_MODE = booleanPreferencesKey("private_mode")
    }

}