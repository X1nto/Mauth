package com.xinto.mauth.domain.otp

import com.xinto.mauth.core.otp.generator.OtpGenerator
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.core.otp.parser.OtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import com.xinto.mauth.core.otp.transformer.KeyTransformer
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.domain.account.model.DomainAccountInfo
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest
import java.util.*

class OtpRepository(
    private val accountsDao: AccountsDao,
    private val rtdataDao: RtdataDao,
    private val otpGenerator: OtpGenerator,
    private val otpKeyTransformer: KeyTransformer,
    private val otpUriParser: OtpUriParser
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getOtpRealtimeData(): Flow<Map<UUID, DomainOtpRealtimeData>> {
        return combine(accountsDao.observeAll(), rtdataDao.observeCountData()) { one, two ->
            Pair(
                one.associateBy { it.id },
                two.associateBy { it.accountId }
            )
        }.transformLatest {(accounts, counters) ->
            while (true) {
                val realtimeData = accounts.mapValues { (id, account) ->
                    val bytes = otpKeyTransformer.transformToBytes(account.secret)
                    when (account.type) {
                        OtpType.HOTP -> {
                            val count = counters[id]!!.count
                            DomainOtpRealtimeData.Hotp(
                                code = otpGenerator.generateHotp(
                                    secret = bytes,
                                    counter = count.toLong(),
                                    digits = account.digits,
                                    digest = account.algorithm
                                ),
                                count = count
                            )
                        }
                        OtpType.TOTP -> {
                            val seconds = System.currentTimeMillis() / 1000
                            val diff = seconds % account.period
                            val progress = 1f - (diff / account.period.toFloat())
                            val countdown = account.period - diff
                            DomainOtpRealtimeData.Totp(
                                code = otpGenerator.generateTotp(
                                    secret = bytes,
                                    interval = account.period.toLong(),
                                    seconds = seconds,
                                    digits = account.digits,
                                    digest = account.algorithm
                                ),
                                progress = progress,
                                countdown = countdown.toInt()
                            )
                        }
                    }
                }
                emit(realtimeData)
                delay(1000)
            }
        }
    }

    fun parseUriToAccountInfo(uri: String): DomainAccountInfo? {
        return when (val parseResult = otpUriParser.parseOtpUri(uri)) {
            is OtpUriParserResult.Success -> {
                DomainAccountInfo.DEFAULT.copy(
                    label = parseResult.data.label,
                    issuer = parseResult.data.issuer,
                    secret = parseResult.data.secret,
                    algorithm = parseResult.data.algorithm,
                    type = parseResult.data.type,
                    digits = parseResult.data.digits.toString(),
                    counter = parseResult.data.counter?.toString() ?: DomainAccountInfo.DEFAULT.counter,
                    period = parseResult.data.period?.toString() ?: DomainAccountInfo.DEFAULT.period,
                )
            }
            is OtpUriParserResult.Failure -> null
        }
    }
}