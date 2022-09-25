package com.xinto.mauth.db.converter

import androidx.room.TypeConverter
import com.xinto.mauth.otp.OtpDigest
import com.xinto.mauth.otp.OtpType

class OtpConverter {

    @TypeConverter
    fun fromIntToDigest(value: Int): OtpDigest {
        return OtpDigest.values()[value]
    }

    @TypeConverter
    fun fromDigestToInt(digest: OtpDigest): Int {
        return digest.ordinal
    }

    @TypeConverter
    fun fromIntToType(value: Int): OtpType {
        return OtpType.values()[value]
    }

    @TypeConverter
    fun fromTypeToInt(digest: OtpType): Int {
        return digest.ordinal
    }

}