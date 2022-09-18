package com.xinto.mauth.otp

enum class OtpDigest(val algorithmName: String) {
    Sha1("HmacSHA1"),
    Sha256("HmacSHA256"),
    Sha512("HmacSHA512")
}