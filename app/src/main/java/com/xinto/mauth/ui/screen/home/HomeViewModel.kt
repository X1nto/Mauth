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
import com.xinto.mauth.Mauth
import com.xinto.mauth.R
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import com.xinto.mauth.core.settings.Settings
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.domain.QrRepository
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.group.GroupRepository
import com.xinto.mauth.domain.group.model.GroupFilter
import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.util.catchMap
import kotlinx.collections.immutable.adapters.ImmutableListAdapter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    application: Application,

    private val settings: Settings,
    private val accounts: AccountRepository,
    private val otp: OtpRepository,
    private val qr: QrRepository,
    private val groupRepository: GroupRepository
) : AndroidViewModel(application) {

    private val _activeGroup = MutableStateFlow<GroupFilter>(GroupFilter.All)
    val activeGroup = _activeGroup.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = _activeGroup
        .flatMapLatest { accounts.getAccounts(it) }
        .map {
            when (it.isNotEmpty()) {
                true -> HomeScreenState.Success(ImmutableListAdapter(it))
                false -> HomeScreenState.Empty
            }
        }.catchMap {
            HomeScreenState.Error(it.localizedMessage ?: it.message ?: it.stackTraceToString())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeScreenState.Loading
        )

    val searchAccounts = accounts.getAccounts(GroupFilter.All)
        .map { ImmutableListAdapter(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    val groups = groupRepository.getGroups()
        .map { ImmutableListAdapter(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    private val _selectedAccounts = MutableStateFlow(listOf<UUID>())
    val selectedAccounts = _selectedAccounts.asStateFlow()

    init {
        viewModelScope.launch {
            groupRepository.getGroups().collect { live ->
                val current = _activeGroup.value
                when {
                    live.isEmpty() && current != GroupFilter.All -> _activeGroup.value = GroupFilter.All
                    current is GroupFilter.Specific && live.none { it.id == current.id } -> _activeGroup.value = GroupFilter.All
                }
            }
        }
    }

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
        val clipboardService = application.getSystemService<ClipboardManager>() ?: return
        val clipData = ClipData.newPlainText(label, code).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description.extras = PersistableBundle().apply {
                    putBoolean("android.content.extra.IS_SENSITIVE", !visible)
                }
            }
        }
        clipboardService.setPrimaryClip(clipData)
        Toast.makeText(application, R.string.home_code_copy_success, Toast.LENGTH_LONG).show()
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
        if (uri == null) return null

        val application = getApplication<Mauth>()

        val contentResolver = application.contentResolver
        val bitmap = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
        }

        val text = qr.decodeQrImage(bitmap)
        if (text == null) {
            Toast.makeText(application, R.string.home_image_parse_fail, Toast.LENGTH_SHORT).show()
            return null
        }

        val parseResult = otp.parseUri(text)
        if (parseResult !is OtpUriParserResult.Success)
            return null

        return with(accounts) {
            parseResult.data.toAccountInfo()
        }
    }

    fun setActiveSort(value: SortSetting) {
        viewModelScope.launch {
            settings.setSortMode(value)
        }
    }

    fun setActiveGroup(filter: GroupFilter) {
        _activeGroup.update { filter }
        clearAccountSelection()
    }

    fun moveSelectedToGroup(groupId: UUID?) {
        viewModelScope.launch {
            groupRepository.assignAccountsToGroup(selectedAccounts.value.toSet(), groupId)
            clearAccountSelection()
        }
    }

    fun createGroup(name: String, emoji: String?) {
        viewModelScope.launch {
            groupRepository.createGroup(name, emoji)
        }
    }

    fun createGroupAndMoveSelected(name: String, emoji: String?) {
        viewModelScope.launch {
            val groupId = groupRepository.createGroup(name, emoji)
            groupRepository.assignAccountsToGroup(selectedAccounts.value.toSet(), groupId)
            clearAccountSelection()
        }
    }
}