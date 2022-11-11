package com.xinto.mauth.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.Mauth
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType
import com.xinto.mauth.ui.navigation.AddAccountParams
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class AddEditAccountViewModel(
    application: Application,
    private val accountsDao: AccountsDao
) : AndroidViewModel(application) {

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    fun updateImageUri(imageUri: Uri?) {
        this.imageUri = imageUri
        if (imageUri != null) {
            val contentResolver = getApplication<Mauth>().contentResolver
            contentResolver.takePersistableUriPermission(
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    var label by mutableStateOf("")
        private set

    var errorLabel by mutableStateOf(false)
        private set

    fun updateLabel(label: String) {
        this.label = label
    }

    var issuer by mutableStateOf("")
        private set

    fun updateIssuer(issuer: String) {
        this.issuer = issuer
    }

    var secret by mutableStateOf("")
        private set

    fun updateSecret(secret: String) {
        this.secret = secret
    }

    var algorithm by mutableStateOf(OtpDigest.Sha1)
        private set

    fun updateAlgorithm(algorithm: OtpDigest) {
        this.algorithm = algorithm
    }

    var type by mutableStateOf(OtpType.Totp)
        private set

    fun updateType(type: OtpType) {
        this.type = type
    }

    var digits by mutableStateOf("6")
        private set

    var errorDigits by mutableStateOf(false)
        private set

    fun updateDigits(digits: String) {
        this.digits = digits
    }

    var counter by mutableStateOf("0")
        private set

    var errorCounter by mutableStateOf(false)
        private set

    fun updateCounter(counter: String) {
        this.counter = counter
    }

    var period by mutableStateOf("30")
        private set

    var errorPeriod by mutableStateOf(false)
        private set

    fun updatePeriod(period: String) {
        this.period = period
    }

    var id by mutableStateOf<UUID?>(null)
        private set

    fun fromId(id: UUID) {
        val account = runBlocking {
            accountsDao.getById(id)
        } ?: return
        resetErrors()
        imageUri = account.icon
        label = account.label
        issuer = account.issuer
        secret = account.secret
        algorithm = account.algorithm
        type = account.type
        digits = account.digits.toString()
        counter = account.counter.toString()
        period = account.period.toString()
        this.id = id
    }

    fun fromParams(params: AddAccountParams) {
        resetErrors()
        imageUri = null
        label = params.label
        issuer = params.issuer
        secret = params.secret
        algorithm = params.algorithm
        type = params.type
        digits = params.digits.toString()
        counter = params.counter.toString()
        period = params.period.toString()
        id = null
    }

    fun save(): Boolean {
        if (label == "") {
            errorLabel = true
            return false
        }

        val digits = digits.toIntOrNull()
        if (digits == null) {
            errorDigits = true
            return false
        }

        val counter = counter.toIntOrNull()
        if (counter == null) {
            errorCounter = true
            return false
        }

        val period = period.toIntOrNull()
        if (period == null || period == 0) {
            errorPeriod = true
            return false
        }

        resetErrors()

        viewModelScope.launch {
            val account = EntityAccount(
                id = id ?: UUID.randomUUID(),
                secret = secret,
                icon = imageUri,
                label = label,
                issuer = issuer,
                algorithm = algorithm,
                type = type,
                digits = digits,
                counter = counter,
                period = period
            )

            if (id != null) {
                accountsDao.update(account)
            } else {
                accountsDao.insert(account)
            }
        }
        return true
    }

    private fun resetErrors() {
        errorLabel = false
        errorDigits = false
        errorCounter = false
        errorPeriod = false
    }
}