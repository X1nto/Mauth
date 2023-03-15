package com.xinto.mauth.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.settings.usecase.GetSecureModeUsecase
import com.xinto.mauth.domain.settings.usecase.SetSecureModeUsecase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    getSecureModeUsecase: GetSecureModeUsecase,
    private val setSecureModeUsecase: SetSecureModeUsecase
) : ViewModel() {

    var secureMode by mutableStateOf(false)
        private set

    private val getSecureModeJob = getSecureModeUsecase()
        .onEach {
            secureMode = it
        }
        .launchIn(viewModelScope)

    fun updateSecureMode(newSecureMode: Boolean) {
        viewModelScope.launch {
            setSecureModeUsecase(newSecureMode)
        }
    }

    override fun onCleared() {
        getSecureModeJob.cancel()
    }
}