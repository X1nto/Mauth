package com.xinto.mauth.domain.settings.usecase

import com.xinto.mauth.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSecureModeUsecase(private val repository: SettingsRepository) {

    operator fun invoke(): Flow<Boolean> {
        return repository.getSecureMode()
    }
}