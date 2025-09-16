package com.xinto.mauth.core.otp.exporter

import com.xinto.mauth.core.otp.model.OtpData

interface OtpExporter {

    fun exportOtp(data: OtpData): String

    fun exportBatch(data: List<OtpData>): List<String>

}