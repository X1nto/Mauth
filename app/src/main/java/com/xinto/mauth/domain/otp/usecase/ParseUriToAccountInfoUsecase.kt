package com.xinto.mauth.domain.otp.usecase

import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.OtpRepository

class ParseUriToAccountInfoUsecase(
    private val repository: OtpRepository
) {

    operator fun invoke(uri: String): DomainAccountInfo? {
        return repository.parseUriToAccountInfo(uri)
    }

}