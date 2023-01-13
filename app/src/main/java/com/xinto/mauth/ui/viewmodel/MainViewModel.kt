package com.xinto.mauth.ui.viewmodel

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.domain.model.DomainAccountInfo
import com.xinto.mauth.domain.repository.MainRepository
import com.xinto.mauth.otp.parser.OtpUriParser
import com.xinto.mauth.otp.parser.OtpUriParserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    mainRepository: MainRepository,
    private val optParser: OtpUriParser,
) : ViewModel() {

    val privateMode = mainRepository.observeSecureMode()

    var optUri by mutableStateOf<DomainAccountInfo?>(null)
        private set

    fun handleIntentData(data: Intent) {
        viewModelScope.launch(Dispatchers.Default) {
            data.data?.let { uri ->
                when (val result = optParser.parseOtpUri(uri.toString())) {
                    is OtpUriParserResult.Success -> {
                        DomainAccountInfo(
                            id = null,
                            icon = null,
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
                    is OtpUriParserResult.Failure -> null
                }.also {
                    optUri = it
                }
            }
        }
    }

    fun onUriHandled() {
        optUri = null
    }
}
