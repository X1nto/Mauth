package com.xinto.mauth.db.dao.group.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class EntityGroup(
    @PrimaryKey
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.BLOB)
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "emoji")
    val emoji: String? = null,

    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int
)
