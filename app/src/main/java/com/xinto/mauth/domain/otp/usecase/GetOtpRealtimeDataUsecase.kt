package com.xinto.mauth.domain.otp.usecase

import com.xinto.mauth.domain.otp.OtpRepository
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import kotlinx.coroutines.flow.Flow
import java.util.*

class GetOtpRealtimeDataUsecase(
    private val repository: OtpRepository
) {

    operator fun invoke(): Flow<Map<UUID, DomainOtpRealtimeData>> {
        return repository.getOtpRealtimeData()
    }

}