package com.xinto.mauth.domain.qr

import android.graphics.Bitmap

interface QrRepository {

    fun decodeQrImage(image: Bitmap): String?

}