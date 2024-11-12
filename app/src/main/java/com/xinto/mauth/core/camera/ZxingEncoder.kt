package com.xinto.mauth.core.camera

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ZxingEncoder {

    private val writer = MultiFormatWriter()

    suspend fun encodeToBitmap(
        data: String,
        size: Int,
        @ColorInt backgroundColor: Int,
        @ColorInt dataColor: Int
    ): Bitmap {
        return withContext(Dispatchers.IO){
            suspendCoroutine { continuation ->
                val bitMatrix = writer.encode(
                    /* contents = */ data,
                    /* format = */ BarcodeFormat.QR_CODE,
                    /* width = */ size,
                    /* height = */ size,
                    /* hints = */ mapOf(EncodeHintType.MARGIN to 2)
                )
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
                    for (x in 0 until size) {
                        for (y in 0 until size) {
                            val hasData = bitMatrix.get(x, y)
                            setPixel(x, y, if (hasData) dataColor else backgroundColor)
                        }
                    }
                }
                continuation.resume(bitmap)
            }
        }
    }
}