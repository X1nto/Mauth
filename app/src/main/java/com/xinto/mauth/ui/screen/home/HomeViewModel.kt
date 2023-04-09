package com.xinto.mauth.ui.screen.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.R
import com.xinto.mauth.Mauth
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.account.usecase.DeleteAccountsUsecase
import com.xinto.mauth.domain.account.usecase.GetAccountsUsecase
import com.xinto.mauth.domain.account.usecase.IncrementAccountCounterUsecase
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.domain.otp.usecase.GetOtpRealtimeDataUsecase
import com.xinto.mauth.domain.otp.usecase.ParseUriToAccountInfoUsecase
import com.xinto.mauth.domain.qr.usecase.DecodeQrImageUsecase
import com.xinto.mauth.domain.settings.model.SortSetting
import com.xinto.mauth.domain.settings.usecase.GetSortModeUsecase
import com.xinto.mauth.domain.settings.usecase.SetSortModeUsecase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    application: Application,

    getAccounts: GetAccountsUsecase,
    getOtpRealtimeData: GetOtpRealtimeDataUsecase,
    private val incrementAccountCounter: IncrementAccountCounterUsecase,
    private val parseUriToAccountInfo: ParseUriToAccountInfoUsecase,
    private val deleteAccounts: DeleteAccountsUsecase,
    private val decodeQrImage: DecodeQrImageUsecase,

    getSortModeUsecase: GetSortModeUsecase,
    private val setSortModeUsecase: SetSortModeUsecase,
) : AndroidViewModel(application) {

    var state by mutableStateOf<HomeScreenState>(HomeScreenState.Loading)
        private set

    val selectedAccounts = mutableStateListOf<UUID>()
    val realtimeData = mutableStateMapOf<UUID, DomainOtpRealtimeData>()

    var activeSortSetting by mutableStateOf(SortSetting.DEFAULT)
        private set

    private val stateJob = getAccounts()
        .catch {
            state = HomeScreenState.Error(
                it.localizedMessage ?: it.message ?: it.stackTraceToString()
            )
        }.onEach {
            state = when (it.isNotEmpty()) {
                true -> HomeScreenState.Success(it)
                false -> HomeScreenState.Empty
            }
        }.launchIn(viewModelScope)

    private val rtdataJob = getOtpRealtimeData()
        .onEach {
            realtimeData.putAll(it)
        }.launchIn(viewModelScope)

    private val sortModeJob = getSortModeUsecase()
        .onEach {
            activeSortSetting = it
        }.launchIn(viewModelScope)

    fun copyCodeToClipboard(label: String, code: String) {
        val application = getApplication<Mauth>()
        val clipboardService = application.getSystemService<ClipboardManager>()
        if (clipboardService != null) {
            clipboardService.setPrimaryClip(ClipData.newPlainText(label, code))
            Toast.makeText(application, R.string.home_code_copy_success, Toast.LENGTH_LONG).show()
        }
    }

    fun toggleAccountSelection(id: UUID) {
        if (selectedAccounts.contains(id)) {
            selectedAccounts.remove(id)
        } else {
            selectedAccounts.add(id)
        }
    }

    fun clearAccountSelection() {
        selectedAccounts.clear()
    }

    fun deleteSelectedAccounts() {
        viewModelScope.launch {
            deleteAccounts(selectedAccounts)
            selectedAccounts.clear()
        }
    }

    fun incrementCounter(accountId: UUID) {
        viewModelScope.launch {
            incrementAccountCounter(accountId)
        }
    }

    fun getAccountInfoFromQrUri(uri: Uri?): DomainAccountInfo? {
        val application = getApplication<Mauth>()

        if (uri != null) {
            val contentResolver = application.contentResolver
            val bitmap = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
            }

            val text = decodeQrImage(bitmap)
            if (text != null) {
                return parseUriToAccountInfo(text)
            }
        }

        Toast.makeText(application, "Failed to decode the QR code.", Toast.LENGTH_SHORT)
            .show()
        return null
    }

    fun setActiveSort(value: SortSetting) {
        viewModelScope.launch {
            setSortModeUsecase(value)
        }
    }

    override fun onCleared() {
        stateJob.cancel()
        rtdataJob.cancel()
        sortModeJob.cancel()
    }

}