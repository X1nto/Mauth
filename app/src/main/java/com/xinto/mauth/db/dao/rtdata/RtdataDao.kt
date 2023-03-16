package com.xinto.mauth.db.dao.rtdata

import androidx.room.*
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RtdataDao {

    @Query("SELECT * FROM countdata")
    fun observeCountData(): Flow<List<EntityCountData>>

    @Query("SELECT * FROM countdata WHERE account_id = :accountId")
    fun observeAccountCountData(accountId: UUID): Flow<EntityCountData>

    @Query("SELECT count FROM countdata WHERE account_id = :accountId")
    suspend fun getAccountCounter(accountId: UUID): Int

    @Upsert
    suspend fun upsertCountData(countData: EntityCountData)

    @Query("UPDATE countdata SET count = count + 1 WHERE account_id = :accountId")
    suspend fun incrementAccountCounter(accountId: UUID)

    @Query("UPDATE countdata SET count = :counter WHERE account_id = :accountId")
    suspend fun setAccountCounter(accountId: UUID, counter: Int)

}