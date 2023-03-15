package com.xinto.mauth.domain.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getSecureMode(): Flow<Boolean>

    suspend fun setSecureMode(value: Boolean)

}