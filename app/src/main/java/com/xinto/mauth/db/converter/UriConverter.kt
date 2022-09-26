package com.xinto.mauth.db.converter

import android.net.Uri
import androidx.room.TypeConverter

class UriConverter {

    @TypeConverter
    fun convertUriToString(uri: Uri): String {
        return uri.toString()
    }

    @TypeConverter
    fun convertStringToUri(uriString: String): Uri {
        return Uri.parse(uriString)
    }

}