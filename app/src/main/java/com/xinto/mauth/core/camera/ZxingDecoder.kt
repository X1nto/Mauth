package com.xinto.mauth.core.camera

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

    inline fun <T> decodeYuvLuminanceSource(
        data: ByteArray,
        dataWidth: Int,
        dataHeight: Int,
        width: Int = dataWidth,
        height: Int = dataHeight,
        left: Int = 0,
        top: Int = 0,
        reverseHorizontal: Boolean = false,
        onSuccess: (Result) -> T,
        onError: (NotFoundException) -> T
    ): T {
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

        return decodeSource(source, onSuccess, onError)
    }

    inline fun <T> decodeRgbLuminanceSource(
        pixels: IntArray,
        width: Int,
        height: Int,
        onSuccess: (Result) -> T,
        onError: (NotFoundException) -> T
    ): T {
        val source = RGBLuminanceSource(
            /* width = */
            width,
            /* height = */
            height,
            /* pixels = */
            pixels,
        )

        return decodeSource(source, onSuccess, onError)
    }

    inline fun <T> decodeSource(
        source: LuminanceSource,
        onSuccess: (Result) -> T,
        onError: (NotFoundException) -> T
    ): T {
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        return try {
            onSuccess(reader.decodeWithState(bitmap))
        } catch (e: NotFoundException) {
            onError(e)
        }
    }

}