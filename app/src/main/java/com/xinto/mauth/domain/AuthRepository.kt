@file:Suppress("DEPRECATION") // AndroidX Crypto

package com.xinto.mauth.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth",
        MasterKey(context = context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val isProtected: StateFlow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CODE) {
                trySend(prefs.contains(KEY_CODE))
            }
        }
        send(prefs.contains(KEY_CODE))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = prefs.contains(KEY_CODE)
    )

    suspend fun validate(code: String): Boolean {
        return withContext(Dispatchers.IO) {
            prefs.getString(KEY_CODE, null) == code
        }
    }

    suspend fun updateCode(code: String) {
        withContext(Dispatchers.IO) {
            prefs.edit(commit = true) {
                putString(KEY_CODE, code)
            }
        }
    }

    suspend fun removeCode() {
        withContext(Dispatchers.IO) {
            prefs.edit(commit = true) {
                remove(KEY_CODE)
            }
        }
    }

    private companion object {
        const val KEY_CODE = "code"
    }
}
