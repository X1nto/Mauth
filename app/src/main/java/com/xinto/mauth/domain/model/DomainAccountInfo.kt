package com.xinto.mauth.domain.model

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Immutable
@Parcelize
data class DomainAccountInfo(
    val id: UUID?,
    val icon: Uri?,
    val label: String,
    val issuer: String,
    val secret: String,
    val algorithm: OtpDigest,
    val type: OtpType,
    val digits: Int,
    val counter: Int,
    val period: Int,
) : Parcelable {

    companion object {
        val DEFAULT = DomainAccountInfo(
            id = null,
            icon = null,
            label = "",
            issuer = "",
            secret = "",
            algorithm = OtpDigest.Sha1,
            type = OtpType.Totp,
            digits = 6,
            counter = 0,
            period = 30
        )
    }

}