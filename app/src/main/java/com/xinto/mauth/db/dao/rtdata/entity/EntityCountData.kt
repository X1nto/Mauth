package com.xinto.mauth.db.dao.rtdata.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "countdata")
data class EntityCountData(
    @PrimaryKey
    @ColumnInfo(name = "account_id")
    val accountId: UUID,

    @ColumnInfo(name = "count")
    val count: Int,
)