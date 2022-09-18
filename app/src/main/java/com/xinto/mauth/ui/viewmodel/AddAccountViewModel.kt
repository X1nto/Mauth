package com.xinto.mauth.ui.viewmodel

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinto.mauth.db.AccountDatabase
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddAccountParams(
    val label: String = "",
    val issuer: String = "",
    val secret: String = "",
) : Parcelable

class AddAccountViewModel(
    private val params: AddAccountParams,
    private val accountsDao: AccountsDao
) : ViewModel() {

    var label by mutableStateOf(params.label)
        private set

    var issuer by mutableStateOf(params.issuer)
        private set

    var secret by mutableStateOf(params.secret)
        private set

    fun updateLabel(label: String) {
        this.label = label
    }

    fun updateIssuer(issuer: String) {
        this.issuer = issuer
    }

    fun updateSecret(secret: String) {
        this.secret = secret
    }

    fun save() {
        viewModelScope.launch {
            accountsDao.insert(
                EntityAccount(
                    secret = secret,
                    label = label,
                    issuer = issuer
                )
            )
        }
    }

}