package com.xinto.mauth.domain.qr.usecase

import android.graphics.Bitmap
import com.xinto.mauth.domain.qr.QrRepository

class DecodeQrImageUsecase(
    private val repository: QrRepository
) {

    operator fun invoke(image: Bitmap): String? {
        return repository.decodeQrImage(image)
    }

}