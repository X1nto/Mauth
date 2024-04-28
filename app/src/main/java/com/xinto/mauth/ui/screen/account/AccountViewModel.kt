package com.xinto.mauth.ui.screen.account

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.domain.account.AccountRepository
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed interface AccountViewModelParams {
    @JvmInline
    value class Id(val id: UUID) : AccountViewModelParams

    @JvmInline
    value class Prefilled(val accountInfo: DomainAccountInfo) : AccountViewModelParams
}

class AccountViewModel(
    application: Application,

    params: AccountViewModelParams,
    private val accounts: AccountRepository
) : AndroidViewModel(application) {

    private val _initialInfo = MutableStateFlow<DomainAccountInfo?>(null)

    private val _state = MutableStateFlow<AccountScreenState>(AccountScreenState.Loading)
    val state = _state.asStateFlow()

    val hasChanges = combine(_initialInfo, state) { initialInfo, state ->
        state is AccountScreenState.Success && state.info != initialInfo
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val canSave = state.map {
        it is AccountScreenState.Success && it.info.isValid()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        when (params) {
            is AccountViewModelParams.Id -> {
                accounts.getAccountInfo(params.id)
                    .onEach {
                        _initialInfo.value = it
                        _state.value = AccountScreenState.Success(it)
                    }.catch {
                        _state.value = AccountScreenState.Error(it.localizedMessage ?: it.message ?: it.stackTraceToString())
                    }.launchIn(viewModelScope)
            }
            is AccountViewModelParams.Prefilled -> {
                _initialInfo.value = params.accountInfo
                _state.value = AccountScreenState.Success(params.accountInfo)
            }
        }
    }
    
    private inline fun mutateState(mutation: (DomainAccountInfo) -> DomainAccountInfo) {
        _state.update {
            if (it !is AccountScreenState.Success) return@update it

            AccountScreenState.Success(mutation(it.info))
        }
    }

    fun updateIcon(uri: Uri?) {
        if (uri == null) return

        val state = state.value
        if (state !is AccountScreenState.Success) return

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                val contentResolver = context.contentResolver
                val bitmap = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
                }

                val destination = File(context.filesDir, "${state.info.id}_${UUID.randomUUID()}.png").apply {
                    if (exists()) {
                        delete()
                    }
                    createNewFile()
                }
                destination.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100,  out)
                }
                state.info.icon?.toFile()?.delete()
                mutateState { it.copy(icon = destination.toUri()) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeIcon() {
        mutateState {
            it.copy(icon = null)
        }
    }

    fun updateLabel(label: String) {
        mutateState {
            it.copy(label = label)
        }
    }

    fun updateIssuer(issuer: String) {
        mutateState {
            it.copy(issuer = issuer)
        }
    }

    fun updateSecret(secret: String) {
        mutateState {
            it.copy(secret = secret)
        }
    }

    fun updateType(otpType: OtpType) {
        mutateState {
            it.copy(type = otpType)
        }
    }

    fun updateDigest(otpDigest: OtpDigest) {
        mutateState {
            it.copy(algorithm = otpDigest)
        }
    }

    fun updateDigits(digits: String) {
        mutateState {
            it.copy(digits = digits)
        }
    }

    fun updateCounter(counter: String) {
        mutateState {
            it.copy(counter = counter)
        }
    }

    fun updatePeriod(period: String) {
        mutateState {
            it.copy(period = period)
        }
    }

    fun saveData() {
        val state = state.value
        if (state is AccountScreenState.Success) {
            viewModelScope.launch {
                accounts.putAccount(state.info)
            }
        }
    }
}