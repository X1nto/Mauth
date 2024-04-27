package com.xinto.mauth.core.camera

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

object ZxingEncoder {

    val writer = MultiFormatWriter()

    fun encodeToBitmap(data: String, size: Int): Bitmap {
        val bitMatrix = writer.encode(
            /* contents = */ data,
            /* format = */ BarcodeFormat.CODE_128,
            /* width = */ size,
            /* height = */ size

        )
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            for (x in 0..size) {
                for (y in 0..size) {
                    val hasData = bitMatrix.get(x, y)
                    setPixel(x, y, if (hasData) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}