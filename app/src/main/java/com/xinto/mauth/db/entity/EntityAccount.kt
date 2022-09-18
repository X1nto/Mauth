package com.xinto.mauth.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class EntityAccount(
    @PrimaryKey
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.BLOB)
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "secret")
    val secret: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "issuer")
    val issuer: String
)
