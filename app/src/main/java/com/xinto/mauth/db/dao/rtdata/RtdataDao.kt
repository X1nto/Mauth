package com.xinto.mauth.db.dao.rtdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface RtdataDao {

    @Query("SELECT * FROM rtdata")
    fun observeCountData(): Flow<List<EntityCountData>>

    @Query("SELECT * FROM rtdata WHERE account_id = :accountId")
    fun observeAccountCountData(accountId: UUID): Flow<EntityCountData>

    @Query("SELECT count FROM rtdata WHERE account_id = :accountId")
    suspend fun getAccountCounter(accountId: UUID): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountData(countData: EntityCountData)

    @Query("UPDATE rtdata SET count = count + 1 WHERE account_id = :accountId")
    suspend fun incrementAccountCounter(accountId: UUID)

    @Query("UPDATE rtdata SET account_id = :counter WHERE account_id = :accountId")
    suspend fun setAccountCounter(accountId: UUID, counter: Int)

}