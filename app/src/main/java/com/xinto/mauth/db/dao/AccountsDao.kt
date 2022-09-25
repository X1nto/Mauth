package com.xinto.mauth.db.dao

import androidx.room.*
import com.xinto.mauth.db.entity.EntityAccount
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts")
    fun observeAll(): Flow<List<EntityAccount>>

    @Query("SELECT * FROM accounts")
    suspend fun getAll(): List<EntityAccount>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: UUID): EntityAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entityAccount: EntityAccount)

    @Update
    suspend fun update(entityAccount: EntityAccount)

    @Delete
    suspend fun delete(entityAccount: EntityAccount)

}