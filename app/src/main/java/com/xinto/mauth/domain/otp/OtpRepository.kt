package com.xinto.mauth.domain.otp

import com.xinto.mauth.core.otp.generateHotp
import com.xinto.mauth.core.otp.generateTotp
import com.xinto.mauth.util.Base32
import com.xinto.mauth.core.otp.OtpType
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.domain.otp.model.DomainOtpRealtimeData
import com.xinto.mauth.domain.otp.model.OtpSpec
import com.xinto.mauth.util.combineTransformLatest
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration.Companion.seconds

class OtpRepository(
    private val accountsDao: AccountsDao,
    private val rtdataDao: RtdataDao
) {

    fun getOtpRealtimeData(): Flow<Map<UUID, DomainOtpRealtimeData>> {
        return combineTransformLatest(accountsDao.observeAll(), rtdataDao.observeCountData()) { accounts, counts ->
            emitRealtimeData(
                specs = accounts.map {
                    OtpSpec(
                        id = it.id,
                        secret = it.secret,
                        digits = it.digits,
                        algorithm = it.algorithm,
                        type = it.type,
                        period = it.period
                    )
                },
                counters = counts.associate { it.accountId to it.count }
            )
        }.flowOn(Dispatchers.Default)
    }

    fun getOtpRealtimeData(
        specs: List<OtpSpec>,
        counters: Map<UUID, Int>
    ): Flow<Map<UUID, DomainOtpRealtimeData>> {
        return flow {
            emitRealtimeData(specs, counters)
        }.flowOn(Dispatchers.Default)
    }

    private suspend fun FlowCollector<Map<UUID, DomainOtpRealtimeData>>.emitRealtimeData(
        specs: List<OtpSpec>,
        counters: Map<UUID, Int>
    ) {
        val secretBytes = specs.associate { spec ->
            spec.id to Base32.decode(spec.secret)
        }

        val (hotpAccounts, totpAccounts) = specs.partition { it.type == OtpType.HOTP }

        val hotpData = buildMap(hotpAccounts.size) {
            for (account in hotpAccounts) {
                val count = counters[account.id] ?: continue
                put(account.id, DomainOtpRealtimeData.Hotp(
                    code = generateHotp(
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
            val data = buildMap(specs.size) {
                putAll(hotpData)
                for (account in totpAccounts) {
                    val period = account.period.coerceAtLeast(1)
                    val diff = seconds % period
                    put(account.id, DomainOtpRealtimeData.Totp(
                        code = generateTotp(
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
    }
}