package com.xinto.mauth.db.dao.account

import androidx.room.*
import com.xinto.mauth.db.dao.account.entity.EntityAccount
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

    @Upsert
    suspend fun upsert(entityAccount: EntityAccount)

    @Delete
    suspend fun delete(entityAccount: EntityAccount)

    @Query("DELETE FROM accounts WHERE id in (:ids)")
    suspend fun delete(ids: Set<UUID>)

}