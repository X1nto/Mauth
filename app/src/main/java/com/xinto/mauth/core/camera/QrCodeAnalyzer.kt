package com.xinto.mauth.core.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.NotFoundException
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onSuccess: (com.google.zxing.Result) -> Unit,
    private val onFail: (NotFoundException) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        image.use { imageProxy ->
            val data = imageProxy.planes[0].buffer.toByteArray()

            ZxingDecoder.decodeYuvLuminanceSource(
                data = data,
                dataWidth = imageProxy.width,
                dataHeight = imageProxy.height,
                onSuccess = onSuccess,
                onError = onFail
            )
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val bytes = ByteArray(remaining())
        get(bytes)
        return bytes
    }
}