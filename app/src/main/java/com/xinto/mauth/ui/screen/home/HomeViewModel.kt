package com.xinto.mauth.ui.screen.home

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.R
import com.xinto.mauth.Mauth
import com.xinto.mauth.domain.AccountRepository
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.domain.OtpRepository
import com.xinto.mauth.domain.QrRepository
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.core.settings.Settings
import com.xinto.mauth.util.catchMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    application: Application,

    private val settings: Settings,
    private val accounts: AccountRepository,
    private val otp: OtpRepository,
    private val qr: QrRepository
) : AndroidViewModel(application) {

    val state = accounts.getAccounts()
        .map {
            when (it.isNotEmpty()) {
                true -> HomeScreenState.Success(it)
                false -> HomeScreenState.Empty
            }
        }.catchMap {
            HomeScreenState.Error(it.localizedMessage ?: it.message ?: it.stackTraceToString())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeScreenState.Loading
        )

    private val _selectedAccounts = MutableStateFlow(listOf<UUID>())
    val selectedAccounts = _selectedAccounts.asStateFlow()

    val realTimeData = otp.getOtpRealtimeData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val activeSortSetting = settings.getSortMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortSetting.DEFAULT
        )

    fun copyCodeToClipboard(label: String, code: String, visible: Boolean) {
        val application = getApplication<Mauth>()
        val clipboardService = application.getSystemService<ClipboardManager>()
        if (clipboardService != null) {
            clipboardService.setPrimaryClip(
                ClipData.newPlainText(label, code)
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            description.extras = PersistableBundle().apply {
                                putBoolean("android.content.extra.IS_SENSITIVE", !visible)
                            }
                        }
                    }
            )
            Toast.makeText(application, R.string.home_code_copy_success, Toast.LENGTH_LONG).show()
        }
    }

    fun toggleAccountSelection(id: UUID) {
        _selectedAccounts.update {
            if (it.contains(id)) {
                it - id
            } else {
                it + id
            }
        }
    }

    fun clearAccountSelection() {
        _selectedAccounts.update {
            emptyList()
        }
    }

    fun deleteSelectedAccounts() {
        viewModelScope.launch {
            accounts.deleteAccounts(selectedAccounts.value)
            clearAccountSelection()
        }
    }

    fun incrementCounter(accountId: UUID) {
        viewModelScope.launch {
            accounts.incrementAccountCounter(accountId)
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

            val text = qr.decodeQrImage(bitmap)
            if (text != null) {
                return otp.parseUriToAccountInfo(text)
            }
        }

        Toast.makeText(application, "Failed to decode the QR code.", Toast.LENGTH_SHORT)
            .show()
        return null
    }

    fun setActiveSort(value: SortSetting) {
        viewModelScope.launch {
            settings.setSortMode(value)
        }
    }

}