package com.xinto.mauth.ui.navigation

import android.os.Parcelable
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddAccountParams(
    val label: String = "",
    val issuer: String = "",
    val secret: String = "",
    val algorithm: OtpDigest = OtpDigest.Sha1,
    val type: OtpType = OtpType.Totp,
    val digits: Int = 6,
    val counter: Int = 0,
    val period: Int = 30,
) : Parcelable
