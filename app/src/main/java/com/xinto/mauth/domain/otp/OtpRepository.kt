package com.xinto.mauth.domain.otp

import com.xinto.mauth.core.otp.generator.OtpGenerator
import com.xinto.mauth.core.otp.model.OtpType
import com.xinto.mauth.core.otp.parser.OtpUriParser
import com.xinto.mauth.core.otp.parser.OtpUriParserResult
import com.xinto.mauth.core.otp.transformer.KeyTransformer
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

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
        }.transformLatest { (accounts, counters) ->
            val secretBytes = accounts.mapValues { (_, account) ->
                otpKeyTransformer.transformToBytes(account.secret)
            }

            val (hotpAccounts, totpAccounts) = accounts.values.partition { it.type == OtpType.HOTP }

            val hotpData = buildMap(hotpAccounts.size) {
                for (account in hotpAccounts) {
                    val count = counters[account.id]?.count ?: continue
                    put(account.id, DomainOtpRealtimeData.Hotp(
                        code = otpGenerator.generateHotp(
                            secret = secretBytes[account.id]!!,
                            counter = count.toLong(),
                            digits = account.digits,
                            digest = account.algorithm
                        ),
                        count = count
                    ))
                }
            }

            while (true) {
                val seconds = System.currentTimeMillis() / 1000
                val data = buildMap(accounts.size) {
                    putAll(hotpData)
                    for (account in totpAccounts) {
                        val period = account.period.coerceAtLeast(1)
                        val diff = seconds % period
                        put(account.id, DomainOtpRealtimeData.Totp(
                            code = otpGenerator.generateTotp(
                                secret = secretBytes[account.id]!!,
                                interval = period.toLong(),
                                seconds = seconds,
                                digits = account.digits,
                                digest = account.algorithm
                            ),
                            progress = 1f - (diff / period.toFloat()),
                            countdown = (period - diff).toInt()
                        ))
                    }
                }
                emit(data)
                delay(1.seconds)
            }
        }.flowOn(Dispatchers.Default)
    }

    fun parseUri(uri: String): OtpUriParserResult {
        return otpUriParser.parseOtpUri(uri)
    }
}