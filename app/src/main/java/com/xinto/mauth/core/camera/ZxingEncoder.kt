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
        @ColorInt backgroundColor: Int,
        @ColorInt dataColor: Int
    ): Bitmap {
        val bitMatrix = writer.encode(
            /* contents = */ data,
            /* format = */ BarcodeFormat.QR_CODE,
            /* width = */ 0,
            /* height = */ 0,
            /* hints = */ mapOf(EncodeHintType.MARGIN to 0)
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                pixels[row + x] = if (bitMatrix.get(x, y)) dataColor else backgroundColor
            }
        }

        return createBitmap(width, height).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
