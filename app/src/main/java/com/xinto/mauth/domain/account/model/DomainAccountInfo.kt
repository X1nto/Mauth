package com.xinto.mauth.domain.account.model

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Immutable
@Parcelize
data class DomainAccountInfo(
    val id: UUID,
    val icon: Uri?,
    val label: String,
    val issuer: String,
    val secret: String,
    val algorithm: OtpDigest,
    val type: OtpType,
    val digits: String,
    val counter: String,
    val period: String,
    val createdMillis: Long
) : Parcelable {

    fun isValid(): Boolean {
        val labelValid = label.isNotEmpty()
        val secretValid = secret.isNotEmpty()
        val digitsValid = digits.toIntOrNull() != null && digits.toInt() <= 10 && digits.toInt() > 0
        val counterValid = counter.toIntOrNull() != null
        val periodValid = period.toIntOrNull() != null && period.toInt() > 1 && period.toInt() < Int.MAX_VALUE / 1000
        return labelValid && secretValid && digitsValid && counterValid && periodValid
    }

    companion object {
        fun new(): DomainAccountInfo {
            return DomainAccountInfo(
                id = UUID.randomUUID(),
                icon = null,
                label = "",
                issuer = "",
                secret = "",
                algorithm = OtpDigest.SHA1,
                type = OtpType.TOTP,
                digits = "6",
                counter = "0",
                period = "30",
                createdMillis = System.currentTimeMillis()
            )
        }
    }
}