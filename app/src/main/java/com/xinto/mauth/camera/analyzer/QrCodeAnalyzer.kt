package com.xinto.mauth.camera.analyzer

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private inline val onSuccess: (com.google.zxing.Result) -> Unit,
    private inline val onFail: (NotFoundException) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE),
            )
        )
    }

    override fun analyze(image: ImageProxy) {
        image.use { imageProxy ->
            val data = imageProxy.planes[0].buffer.toByteArray()

            val source = PlanarYUVLuminanceSource(
                /* yuvData = */
                data,
                /* dataWidth = */
                imageProxy.width,
                /* dataHeight = */
                imageProxy.height,
                /* left = */
                0,
                /* top = */
                0,
                /* width = */
                imageProxy.width,
                /* height = */
                imageProxy.height,
                /* reverseHorizontal = */
                false,
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                onSuccess(reader.decodeWithState(bitmap))
                Log.d("test", "success")
            } catch (e: NotFoundException) {
                onFail(e)
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val bytes = ByteArray(remaining())
        get(bytes)
        return bytes
    }


}