package com.xinto.mauth.ui.navigation

import android.os.Parcelable
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType
import kotlinx.parcelize.Parcelize

@Parcelize
class AddAccountParams(
    val label: String = "",
    val issuer: String = "",
    val secret: String = "",
    val algorithm: OtpDigest = OtpDigest.Sha1,
    val type: OtpType = OtpType.Totp,
    val digits: Int = 6,
    val counter: Int = 0,
    val period: Int = 30,
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + issuer.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + digits
        result = 31 * result + counter
        result = 31 * result + period
        return result
    }
}
