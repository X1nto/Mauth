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

class AddAccountViewModel(
    application: Application,
    private val params: AddAccountParams,
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

    var label by mutableStateOf(params.label)
        private set

    fun updateLabel(label: String) {
        this.label = label
    }

    var issuer by mutableStateOf(params.issuer)
        private set

    fun updateIssuer(issuer: String) {
        this.issuer = issuer
    }

    var secret by mutableStateOf(params.secret)
        private set

    fun updateSecret(secret: String) {
        this.secret = secret
    }

    var algorithm by mutableStateOf(params.algorithm)
        private set

    fun updateAlgorithm(algorithm: OtpDigest) {
        this.algorithm = algorithm
    }

    var type by mutableStateOf(params.type)
        private set

    fun updateType(type: OtpType) {
        this.type = type
    }

    var digits by mutableStateOf(params.digits)
        private set

    fun updateDigits(digits: Int) {
        this.digits = digits
    }

    fun updateDigits(digits: String) {
        val intDigits = digits.toIntOrNull()
        if (intDigits != null) {
            updateDigits(intDigits)
        }
    }

    var counter by mutableStateOf(params.counter)
        private set

    fun updateCounter(counter: Int) {
        this.counter = counter
    }

    fun updateCounter(counter: String) {
        val intCounter = counter.toIntOrNull()
        if (intCounter != null) {
            updateCounter(intCounter)
        }
    }

    var period by mutableStateOf(params.period)
        private set

    fun updatePeriod(period: Int) {
        this.period = period
    }

    fun updatePeriod(period: String) {
        val intPeriod = period.toIntOrNull()
        if (intPeriod != null) {
            updatePeriod(intPeriod)
        }
    }

    fun reset() {
        label = ""
        issuer = ""
        secret = ""
        algorithm = OtpDigest.Sha1
        type = OtpType.Totp
        digits = 6
        counter = 0
        period = 30
    }

    fun save() {
        viewModelScope.launch {
            accountsDao.insert(
                EntityAccount(
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
            )
            reset()
        }
    }
}