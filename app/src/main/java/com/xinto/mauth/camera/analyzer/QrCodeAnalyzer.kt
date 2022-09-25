package com.xinto.mauth.camera.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.NotFoundException
import com.xinto.mauth.camera.decoder.ZxingDecoder
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private inline val onSuccess: (com.google.zxing.Result) -> Unit,
    private inline val onFail: (NotFoundException) -> Unit
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