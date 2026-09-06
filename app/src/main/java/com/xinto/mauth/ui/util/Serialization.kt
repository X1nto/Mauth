package com.xinto.mauth.ui.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import kotlin.io.encoding.Base64

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> Json.encodeToByteArray(value: T): ByteArray {
    val stream = ByteArrayOutputStream()
    encodeToStream(value, stream)
    return stream.toByteArray()
}

object Base64Bytes : KSerializer<ByteArray> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Base64Bytes", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(Base64.encode(value))
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return Base64.Mime.decode(decoder.decodeString())
    }
}
