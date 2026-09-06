package com.xinto.mauth.core.backup.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object AegisSchema {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }

    const val VAULT_VERSION = 1
    const val DB_VERSION = 3
    const val SLOT_TYPE_PASSWORD = 1
}

@Serializable
data class AegisVault(
    val version: Int,
    val header: AegisHeader,
    val db: JsonElement
)

@Serializable
data class AegisHeader(
    val slots: List<AegisSlot>? = null,
    val params: AegisParams? = null
)

@Serializable
data class AegisSlot(
    val type: Int,
    val uuid: String? = null,
    val key: String,
    @SerialName("key_params")
    val keyParams: AegisParams? = null,
    val n: Int? = null,
    val r: Int? = null,
    val p: Int? = null,
    val salt: String? = null
)

@Serializable
data class AegisParams(
    val nonce: String,
    val tag: String
)

@Serializable
data class AegisGroup(
    val uuid: String,
    val name: String
)

