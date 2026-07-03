package com.xinto.mauth.db.dao.group

import androidx.room.*
import com.xinto.mauth.db.dao.group.entity.EntityGroup
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface GroupsDao {

    @Query("SELECT * FROM `groups` ORDER BY sort_index")
    fun observeAll(): Flow<List<EntityGroup>>

    @Query("SELECT * FROM `groups` WHERE id = :id")
    suspend fun getById(id: UUID): EntityGroup?

    @Query("SELECT COALESCE(MAX(sort_index), -1) + 1 FROM `groups`")
    suspend fun nextSortIndex(): Int

    @Upsert
    suspend fun upsert(group: EntityGroup)

    @Query("UPDATE `groups` SET sort_index = :sortIndex WHERE id = :id")
    suspend fun updateSortIndex(id: UUID, sortIndex: Int)

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("UPDATE accounts SET group_id = NULL WHERE group_id = :id")
    suspend fun ungroupAssociatedAccounts(id: UUID)

    @Transaction
    suspend fun deleteWithMembers(id: UUID) {
        ungroupAssociatedAccounts(id)
        deleteById(id)
    }

}
