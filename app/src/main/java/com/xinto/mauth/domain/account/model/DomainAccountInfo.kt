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
    val digits: Int,
    val counter: Int,
    val period: Int,
    val createdMillis: Long
) : Parcelable {

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
                digits = 6,
                counter = 0,
                period = 30,
                createdMillis = System.currentTimeMillis()
            )
        }
    }
}