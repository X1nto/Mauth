package com.xinto.mauth.domain.settings.usecase

import com.xinto.mauth.domain.settings.SettingsRepository

class SetSecureModeUsecase(private val repository: SettingsRepository) {

    suspend operator fun invoke(value: Boolean) {
        repository.setSecureMode(value)
    }
}