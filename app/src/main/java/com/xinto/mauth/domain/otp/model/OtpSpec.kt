package com.xinto.mauth.domain.otp.model

import com.xinto.mauth.core.otp.OtpDigest
import com.xinto.mauth.core.otp.OtpType
import java.util.UUID

data class OtpSpec(
    val id: UUID,
    val secret: String,
    val digits: Int,
    val algorithm: OtpDigest,
    val type: OtpType,
    val period: Int
)
