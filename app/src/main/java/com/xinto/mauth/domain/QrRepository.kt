package com.xinto.mauth.domain

import android.graphics.Bitmap
import com.xinto.mauth.core.camera.ZxingDecoder

class QrRepository {

    fun decodeQrImage(image: Bitmap): String? {
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        return ZxingDecoder.decodeRgbLuminanceSource(
            pixels = pixels,
            width = image.width,
            height = image.height,
            onSuccess = {
                return it.text
            },
            onError = {
                null
            }
        )
    }
}