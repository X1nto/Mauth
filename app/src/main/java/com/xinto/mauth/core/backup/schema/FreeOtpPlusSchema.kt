package com.xinto.mauth.core.backup.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object FreeOtpPlusSchema {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
}

@Serializable
data class FreeOtpBackup(
    val tokens: List<FreeOtpToken> = emptyList(),
    val tokenOrder: List<String> = emptyList()
)

@Serializable
data class FreeOtpToken(
    val secret: List<Int>? = null,
    @SerialName("secret_base32")
    val secretBase32: String? = null,
    val algo: String? = null,
    val digits: Int? = null,
    val counter: Int? = null,
    val period: Int? = null,
    val issuerExt: String? = null,
    val issuerInt: String? = null,
    val label: String? = null,
    val type: String? = null,
    val category: String? = null
)
