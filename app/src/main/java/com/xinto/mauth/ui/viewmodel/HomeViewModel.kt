package com.xinto.mauth.ui.viewmodel

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
import com.xinto.mauth.Mauth
import com.xinto.mauth.camera.decoder.ZxingDecoder
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.domain.repository.HomeRepository
import com.xinto.mauth.otp.generator.OtpGenerator
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserResult
import com.xinto.mauth.otp.transformer.KeyTransformer
import com.xinto.mauth.ui.navigation.AddAccountParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

class HomeViewModel(
    application: Application,
    private val otpGenerator: OtpGenerator,
    private val keyTransformer: KeyTransformer,
    private val otpUriParser: OtpUriParser,
    private val homeRepository: HomeRepository,
) : AndroidViewModel(application) {

    private val keyBytes = mutableMapOf<String, ByteArray>()

    sealed interface State {
        object Normal : State
        object Loading : State
        object Failed : State
    }
    sealed interface BottomBarState {
        object Normal : BottomBarState
        object Selection : BottomBarState
    }

    var state by mutableStateOf<State>(State.Loading)
        private set
    var bottomBarState by mutableStateOf<BottomBarState>(BottomBarState.Normal)
        private set

    val codes = mutableStateMapOf<UUID, String>()
    val timerProgresses = mutableStateMapOf<UUID, Float>()
    val timerValues = mutableStateMapOf<UUID, Long>()

    val accounts = mutableStateListOf<DomainAccount>()

    val selectedAccounts = mutableStateListOf<UUID>()

    private val totpTimer = fixedRateTimer(name = "totp-timer", daemon = false, period = 1000L) {
        viewModelScope.launch(Dispatchers.Main) {
            accounts.filterIsInstance<DomainAccount.Totp>().forEach {
                generateTotp(it)
            }
        }
    }

    fun copyCodeToClipboard(label: String, code: String?) {
        val application = getApplication<Mauth>()
        if (code != null) {
            val clipboardService = application.getSystemService<ClipboardManager>()
            clipboardService?.setPrimaryClip(ClipData.newPlainText(label, code))
            Toast.makeText(
                application,
                "Successfully copied the code to clipboard",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(application, "Failed to copy: the code is null", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun decodeQrCodeFromImageUri(uri: Uri): String? {
        val application = getApplication<Mauth>()
        val contentResolver = application.contentResolver
        val bitmap = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
        }
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        ZxingDecoder.decodeRgbLuminanceSource(
            pixels = pixels,
            width = bitmap.width,
            height = bitmap.height,
            onSuccess = {
                return it.text
            },
            onError = {
                Toast.makeText(application, "Failed to decode the QR code.", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        return null
    }

    fun parseOtpUri(uri: String): AddAccountParams? {
        val application = getApplication<Mauth>()
        return when (val result = otpUriParser.parseOtpUri(uri)) {
            is OtpUriParserResult.Success -> {
                AddAccountParams(
                    label = result.data.label,
                    issuer = result.data.issuer,
                    secret = result.data.secret,
                    algorithm = result.data.algorithm,
                    type = result.data.type,
                    digits = result.data.digits,
                    counter = result.data.counter ?: 0,
                    period = result.data.period ?: 30,
                )
            }
            is OtpUriParserResult.Failure -> {
                Toast.makeText(application, result.error.toString(), Toast.LENGTH_SHORT).show()
                null
            }
        }
    }

    fun selectUnselectAccount(accountId: UUID) {
        if (selectedAccounts.contains(accountId)) {
            selectedAccounts.remove(accountId)
        } else {
            selectedAccounts.add((accountId))
        }

        bottomBarState = if (selectedAccounts.isEmpty()) {
            BottomBarState.Normal
        } else {
            BottomBarState.Selection
        }
    }

    fun clearSelection() {
        selectedAccounts.clear()
        bottomBarState = BottomBarState.Normal
    }

    fun deleteSelected() {
        val accounts = selectedAccounts.toList()
        clearSelection()
        viewModelScope.launch {
            homeRepository.deleteAccounts(accounts)
        }
    }

    fun incrementAccountCounter(id: UUID) {
        viewModelScope.launch {
            homeRepository.incrementAccountCounter(id)
        }
    }

    override fun onCleared() {
        totpTimer.cancel()
    }

    private fun generateTotp(domainAccount: DomainAccount.Totp) {
        val seconds = System.currentTimeMillis() / 1000
        val keyByte = keyBytes[domainAccount.secret]
        if (keyByte != null) {
            codes[domainAccount.id] = otpGenerator.generateTotp(
                secret = keyByte,
                interval = domainAccount.period.toLong(),
                digits = domainAccount.digits,
                seconds = seconds,
                digest = domainAccount.algorithm
            )
        }
        val diff = seconds % domainAccount.period
        timerProgresses[domainAccount.id] = 1f - (diff / domainAccount.period.toFloat())
        timerValues[domainAccount.id] = domainAccount.period - diff
    }

    private fun generateHotp(domainAccount: DomainAccount.Hotp) {
        val keyByte = keyBytes[domainAccount.secret]
        if (keyByte != null) {
            codes[domainAccount.id] = otpGenerator.generateHotp(
                secret = keyByte,
                counter = domainAccount.counter.toLong(),
                digits = domainAccount.digits,
                digest = domainAccount.algorithm
            )
        }
    }

    init {
        viewModelScope.launch {
            homeRepository.observeAccounts()
                .onEach { domainAccounts ->
                    state = State.Loading

                    accounts.clear()
                    accounts.addAll(domainAccounts)

                    keyBytes.clear()
                    domainAccounts.forEach {
                        keyBytes[it.secret] = keyTransformer.transformToBytes(it.secret)
                    }

                    accounts.filterIsInstance<DomainAccount.Totp>().forEach {
                        generateTotp(it)
                    }
                    accounts.filterIsInstance<DomainAccount.Hotp>().forEach {
                        generateHotp(it)
                    }

                    state = State.Normal
                }
                .catch {
                    state = State.Failed
                }
                .launchIn(this)
        }
    }
}