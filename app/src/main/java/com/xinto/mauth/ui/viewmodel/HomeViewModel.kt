package com.xinto.mauth.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.Mauth
import com.xinto.mauth.R
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.domain.repository.HomeRepository
import com.xinto.mauth.ui.screen.HomeBottomBarState
import com.xinto.mauth.ui.screen.HomeState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    application: Application,
    private val homeRepository: HomeRepository,
) : AndroidViewModel(application) {

    var state by mutableStateOf<HomeState>(HomeState.Loading)
        private set
    var bottomBarState by mutableStateOf<HomeBottomBarState>(HomeBottomBarState.Normal)
        private set

    val accounts = mutableStateListOf<DomainAccount>()
    val selectedAccounts = mutableStateListOf<UUID>()

    val codes = mutableStateMapOf<UUID, String>()
    val timerProgresses = mutableStateMapOf<UUID, Float>()
    val timerValues = mutableStateMapOf<UUID, Long>()

    fun copyCodeToClipboard(label: String, code: String?) {
        val application = getApplication<Mauth>()

        viewModelScope.launch {
            if (homeRepository.copyCodeToClipboard(label, code)) {
                Toast.makeText(application, R.string.home_code_copy_success, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(application, R.string.home_code_copy_fail, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun selectUnselectAccount(accountId: UUID) {
        viewModelScope.launch {
            homeRepository.selectUnselectAccount(accountId)
        }
    }

    fun clearSelection() {
        viewModelScope.launch {
            homeRepository.clearAccountSelection()
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            homeRepository.deleteSelectedAccounts()
        }
    }

    fun incrementAccountCounter(id: UUID) {
        viewModelScope.launch {
            homeRepository.incrementAccountCounter(id)
        }
    }

    fun parseImageUri(uri: Uri?): DomainAccountInfo? {
        val info = homeRepository.decodeImageFromUri(uri)
        if (info == null) {
            Toast.makeText(getApplication(), R.string.home_image_parse_fail, Toast.LENGTH_LONG).show()
        }
        return info
    }

    init {
        homeRepository.observeAccounts()
            .onEach {
                accounts.clear()
                accounts.addAll(it)
            }
            .launchIn(viewModelScope)
        homeRepository.observeSelectedAccounts()
            .onEach {
                selectedAccounts.clear()
                selectedAccounts.addAll(it)

                bottomBarState = if (it.isEmpty()) {
                    HomeBottomBarState.Normal
                } else {
                    HomeBottomBarState.Selection
                }
            }
            .launchIn(viewModelScope)
        homeRepository.observeAccountCodes()
            .onEach {
                codes.clear()
                codes.putAll(it)
            }
            .launchIn(viewModelScope)
        homeRepository.observeTimerProgresses()
            .onEach {
                timerProgresses.clear()
                timerProgresses.putAll(it)
            }
            .launchIn(viewModelScope)
        homeRepository.observeTimerValues()
            .onEach {
                timerValues.clear()
                timerValues.putAll(it)
            }
            .launchIn(viewModelScope)
    }
}