package com.xinto.mauth.core.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DefaultAuthManager(
    context: Context
) : AuthManager {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth",
        MasterKey(context = context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun getCode(): Flow<String?> {
        return callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == KEY_CODE) {
                    trySend(sharedPreferences.getString(KEY_CODE, null))
                }
            }
            send(prefs.getString(KEY_CODE, null))
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    override fun setCode(code: String) {
        prefs.edit {
            putString(KEY_CODE, code)
        }
    }

    override fun removeCode() {
        prefs.edit {
            remove(KEY_CODE)
        }
    }

    private companion object {
        const val KEY_CODE = "code"
    }
}