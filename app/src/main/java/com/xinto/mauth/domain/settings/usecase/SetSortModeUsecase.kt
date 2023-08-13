package com.xinto.mauth.domain.settings.usecase

import com.xinto.mauth.domain.settings.SettingsRepository
import com.xinto.mauth.domain.settings.model.SortSetting

class SetSortModeUsecase(
    val repository: SettingsRepository
) {

    suspend operator fun invoke(value: SortSetting) {
        repository.setSortMode(value)
    }

}