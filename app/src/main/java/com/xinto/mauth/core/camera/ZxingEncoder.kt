package com.xinto.mauth.core.camera

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

object ZxingEncoder {

    private val writer = MultiFormatWriter()

    fun encodeToBitmap(
        data: String,
        size: Int,
        @ColorInt backgroundColor: Int,
        @ColorInt dataColor: Int
    ): Bitmap {
        val bitMatrix = writer.encode(
            /* contents = */ data,
            /* format = */ BarcodeFormat.QR_CODE,
            /* width = */ size,
            /* height = */ size,
            /* hints = */ mapOf(EncodeHintType.MARGIN to 2)
        )

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val row = y * size
            for (x in 0 until size) {
                pixels[row + x] = if (bitMatrix.get(x, y)) dataColor else backgroundColor
            }
        }

        return createBitmap(size, size).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }
}
