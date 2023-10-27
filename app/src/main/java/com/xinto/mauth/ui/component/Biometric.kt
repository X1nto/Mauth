package com.xinto.mauth.ui.component

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

@Composable
fun rememberBiometricHandler(
    onAuthSuccess: () -> Unit = {},
    onAuthFailed: () -> Unit = {},
    onAuthError: (errorCode: Int, errorString: String) -> Unit = {_, _ -> }
): BiometricHandler {
    val context = LocalContext.current
    return remember(context, onAuthSuccess, onAuthFailed, onAuthError) {
        BiometricHandler(
            context = context,
            onAuthSuccess = onAuthSuccess,
            onAuthFailed = onAuthFailed,
            onAuthError = onAuthError
        )
    }
}

@Immutable
class BiometricHandler(
    context: Context,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    onAuthError: (errorCode: Int, errorString: String) -> Unit,
) {

    private val biometricManager = BiometricManager.from(context)
    private val biometricPrompt = BiometricPrompt(
        context as FragmentActivity,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) = onAuthSuccess()

            override fun onAuthenticationFailed() = onAuthFailed()

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) = onAuthError(errorCode, errString.toString())
        }
    )

    fun canUseBiometrics(): Boolean {
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun requestBiometrics(promptData: BiometricPromptData) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptData.title)
            .setSubtitle(promptData.subtitle)
            .setDescription(promptData.description)
            .setNegativeButtonText(promptData.negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun cancelRequest() {
        biometricPrompt.cancelAuthentication()
    }
}

@Composable
fun rememberBiometricPromptData(
    title: String,
    negativeButtonText: String,
    subtitle: String? = null,
    description: String? = null
) : BiometricPromptData {
    return remember(title, negativeButtonText, subtitle, description) {
        BiometricPromptData(
            title = title,
            negativeButtonText = negativeButtonText,
            subtitle = subtitle,
            description = description
        )
    }
}

@Immutable
data class BiometricPromptData(
    val title: String,
    val negativeButtonText: String,
    val subtitle: String? = null,
    val description: String? = null,
)