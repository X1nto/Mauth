package com.xinto.mauth.domain.settings.usecase

import com.xinto.mauth.domain.settings.SettingsRepository
import com.xinto.mauth.domain.settings.model.SortSetting
import kotlinx.coroutines.flow.Flow

class GetSortModeUsecase(
    val repository: SettingsRepository
) {

    operator fun invoke(): Flow<SortSetting> {
        return repository.getSortMode()
    }

}