package com.xinto.mauth.domain.otp

import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import kotlinx.coroutines.flow.Flow
import java.util.*

interface OtpRepository {

    fun getOtpRealtimeData(): Flow<Map<UUID, DomainOtpRealtimeData>>

    fun parseUriToAccountInfo(uri: String): DomainAccountInfo?

}