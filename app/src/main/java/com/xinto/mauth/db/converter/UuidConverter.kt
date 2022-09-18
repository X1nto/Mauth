package com.xinto.mauth.db.converter

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

class UuidConverter {

    @TypeConverter
    fun uuidToBytes(uuid: UUID): ByteArray {
        val bytes = ByteArray(16)

        ByteBuffer.wrap(bytes)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(uuid.mostSignificantBits)
            .putLong(uuid.leastSignificantBits)

        return bytes
    }

    @TypeConverter
    fun bytesToUuid(uuid: ByteArray): UUID {
        val buffer = ByteBuffer.wrap(uuid)

        return UUID(buffer.long, buffer.long)
    }

}