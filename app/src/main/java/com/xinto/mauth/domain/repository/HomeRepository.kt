package com.xinto.mauth.domain.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.getSystemService
import com.xinto.mauth.camera.decoder.ZxingDecoder
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount
import com.xinto.mauth.domain.model.DomainAccount
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.otp.OtpType
import com.xinto.mauth.otp.generator.OtpGenerator
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserResult
import com.xinto.mauth.otp.transformer.KeyTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import java.util.*
import kotlin.concurrent.fixedRateTimer

interface HomeRepository {

    suspend fun getAccounts(): List<DomainAccount>
    suspend fun selectUnselectAccount(id: UUID)
    suspend fun clearAccountSelection()
    suspend fun deleteSelectedAccounts()
    suspend fun incrementAccountCounter(id: UUID)

    suspend fun copyCodeToClipboard(label: String, code: String?): Boolean

    fun decodeImageFromUri(uri: Uri?): DomainAccountInfo?

    fun observeAccounts(): Flow<List<DomainAccount>>
    fun observeSelectedAccounts(): Flow<Set<UUID>>
    fun observeAccountCodes(): Flow<Map<UUID, String>>
    fun observeTimerProgresses(): Flow<Map<UUID, Float>>
    fun observeTimerValues(): Flow<Map<UUID, Long>>

}

class HomeRepositoryImpl(
    private val context: Context,
    private val accountsDao: AccountsDao,
    private val keyTransformer: KeyTransformer,
    private val otpGenerator: OtpGenerator,
    private val otpUriParser: OtpUriParser,
) : HomeRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val keyBytes = mutableMapOf<String, ByteArray>()
    private val accounts = mutableListOf<EntityAccount>()
    private val selectedAccountIds = mutableSetOf<UUID>()

    private val accountCodes = mutableMapOf<UUID, String>()
    private val timerProgresses = mutableMapOf<UUID, Float>()
    private val timerValues = mutableMapOf<UUID, Long>()

    private val selectedAccountsFlow = MutableSharedFlow<Set<UUID>>(1)

    private val accountCodesFlow = MutableSharedFlow<Map<UUID, String>>(1)
    private val timerProgressesFlow = MutableSharedFlow<Map<UUID, Float>>(1)
    private val timerValuesFlow = MutableSharedFlow<Map<UUID, Long>>(1)

    private val timer = fixedRateTimer("null", daemon = true, period = 1000L) {
        accounts.forEach {
            if (it.type == OtpType.Totp) {
                generateTotp(it)
            }
        }

        updateValues()
    }

    override suspend fun getAccounts(): List<DomainAccount> {
        return accountsDao.getAll().map {
            it.toDomain()
        }
    }

    override suspend fun selectUnselectAccount(id: UUID) {
        if (id in selectedAccountIds) {
            selectedAccountIds.remove(id)
        } else {
            selectedAccountIds.add(id)
        }

        selectedAccountsFlow.emit(selectedAccountIds)
    }

    override suspend fun clearAccountSelection() {
        selectedAccountIds.clear()
        selectedAccountsFlow.emit(selectedAccountIds)
    }

    override suspend fun deleteSelectedAccounts() {
        accountsDao.delete(selectedAccountIds)
        clearAccountSelection()
    }

    override suspend fun incrementAccountCounter(id: UUID) {
        val account = accountsDao.getById(id) ?: return
        accountsDao.update(
            account.copy(counter = account.counter + 1)
        )
    }

    override fun observeAccounts(): Flow<List<DomainAccount>> {
        return accountsDao.observeAll().map { entityAccounts ->
            entityAccounts.map {
                it.toDomain()
            }
        }
    }

    override fun observeSelectedAccounts() = selectedAccountsFlow
    override fun observeAccountCodes() = accountCodesFlow

    override fun observeTimerProgresses() = timerProgressesFlow

    override fun observeTimerValues() = timerValuesFlow

    override suspend fun copyCodeToClipboard(label: String, code: String?): Boolean {
        if (code == null) {
            return false
        }

        val clipboardService = context.getSystemService<ClipboardManager>()
            ?: return false

        clipboardService.setPrimaryClip(ClipData.newPlainText(label, code))
        return true
    }

    override fun decodeImageFromUri(uri: Uri?): DomainAccountInfo? {
        if (uri == null) {
            return null
        }

        val contentResolver = context.contentResolver
        val bitmap = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, false)
        }
        val pixels = IntArray(bitmap.width * bitmap.height)
            .also {
                bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            }

        val decodedUri = ZxingDecoder.decodeRgbLuminanceSource(
            pixels = pixels,
            width = bitmap.width,
            height = bitmap.height,
            onSuccess = { it.text },
            onError = { null }
        ) ?: return null

        return when (val parseResult = otpUriParser.parseOtpUri(decodedUri)) {
            is OtpUriParserResult.Success -> {
                DomainAccountInfo(
                    id = null,
                    icon = null,
                    label = parseResult.data.label,
                    issuer = parseResult.data.issuer,
                    secret = parseResult.data.secret,
                    algorithm = parseResult.data.algorithm,
                    type = parseResult.data.type,
                    digits = parseResult.data.digits,
                    counter = parseResult.data.counter ?: 0,
                    period = parseResult.data.period ?: 30,
                )
            }
            is OtpUriParserResult.Failure -> {
                null
            }
        }
    }

    private fun updateValues() {
        accountCodesFlow.tryEmit(accountCodes)
        timerProgressesFlow.tryEmit(timerProgresses)
        timerValuesFlow.tryEmit(timerValues)
    }

    private fun generateTotp(account: EntityAccount) {
        val seconds = System.currentTimeMillis() / 1000
        val keyByte = keyBytes[account.secret]
        if (keyByte != null) {
            accountCodes[account.id] = otpGenerator.generateTotp(
                secret = keyByte,
                interval = account.period.toLong(),
                digits = account.digits,
                seconds = seconds,
                digest = account.algorithm
            )
        }
        val diff = seconds % account.period
        timerProgresses[account.id] = 1f - (diff / account.period.toFloat())
        timerValues[account.id] = account.period - diff
    }

    private fun generateHotp(account: EntityAccount) {
        val keyByte = keyBytes[account.secret]
        if (keyByte != null) {
            accountCodes[account.id] = otpGenerator.generateHotp(
                secret = keyByte,
                counter = account.counter.toLong(),
                digits = account.digits,
                digest = account.algorithm
            )
        }
    }

    private fun EntityAccount.toDomain(): DomainAccount {
        return when (type) {
            OtpType.Totp -> {
                DomainAccount.Totp(
                    id = id,
                    icon = icon,
                    secret = secret,
                    label = label,
                    issuer = issuer,
                    algorithm = algorithm,
                    digits = digits,
                    period = period
                )
            }
            OtpType.Hotp -> {
                DomainAccount.Hotp(
                    id = id,
                    secret = secret,
                    icon = icon,
                    label = label,
                    issuer = issuer,
                    algorithm = algorithm,
                    digits = digits,
                    counter = counter
                )
            }
        }
    }

    init {
        accountsDao.observeAll()
            .onEach { entityAccounts ->
                keyBytes.clear()
                entityAccounts.forEach {
                    keyBytes[it.secret] = keyTransformer.transformToBytes(it.secret)

                    when (it.type) {
                        OtpType.Totp -> {
                            generateTotp(it)
                        }
                        OtpType.Hotp -> {
                            generateHotp(it)
                        }
                    }

                    updateValues()
                }

                accounts.clear()
                accounts.addAll(entityAccounts)
            }
            .launchIn(coroutineScope)
    }

}