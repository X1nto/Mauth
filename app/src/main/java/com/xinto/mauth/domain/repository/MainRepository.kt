package com.xinto.mauth.domain.repository

import com.xinto.mauth.preferences.PreferenceManager
import kotlinx.coroutines.flow.Flow

interface MainRepository {

    fun observeSecureMode(): Flow<Boolean>

}

class MainRepositoryImpl(
    private val preferenceManager: PreferenceManager
) : MainRepository {

    override fun observeSecureMode(): Flow<Boolean> {
        return preferenceManager.observePrivateMode()
    }

}