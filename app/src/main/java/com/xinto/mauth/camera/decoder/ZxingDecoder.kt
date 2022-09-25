package com.xinto.mauth.camera.decoder

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

object ZxingDecoder {

    val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE),
            )
        )
    }

    inline fun decodeYuvLuminanceSource(
        data: ByteArray,
        dataWidth: Int,
        dataHeight: Int,
        width: Int = dataWidth,
        height: Int = dataHeight,
        left: Int = 0,
        top: Int = 0,
        reverseHorizontal: Boolean = false,
        onSuccess: (Result) -> Unit,
        onError: (NotFoundException) -> Unit
    ) {
        val source = PlanarYUVLuminanceSource(
            /* yuvData = */
            data,
            /* dataWidth = */
            dataWidth,
            /* dataHeight = */
            dataHeight,
            /* left = */
            left,
            /* top = */
            top,
            /* width = */
            width,
            /* height = */
            height,
            /* reverseHorizontal = */
            reverseHorizontal,
        )

        decodeSource(source, onSuccess, onError)
    }

    inline fun decodeRgbLuminanceSource(
        pixels: IntArray,
        width: Int,
        height: Int,
        onSuccess: (Result) -> Unit,
        onError: (NotFoundException) -> Unit
    ) {
        val source = RGBLuminanceSource(
            /* width = */
            width,
            /* height = */
            height,
            /* pixels = */
            pixels,
        )

        decodeSource(source, onSuccess, onError)
    }

    inline fun decodeSource(
        source: LuminanceSource,
        onSuccess: (Result) -> Unit,
        onError: (NotFoundException) -> Unit
    ) {
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            onSuccess(reader.decodeWithState(bitmap))
        } catch (e: NotFoundException) {
            onError(e)
        }
    }

}