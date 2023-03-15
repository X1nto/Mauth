package com.xinto.mauth.db.dao.account.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.xinto.mauth.core.otp.model.OtpDigest
import com.xinto.mauth.core.otp.model.OtpType
import java.util.*

@Entity(tableName = "accounts")
data class EntityAccount(
    @PrimaryKey
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.BLOB)
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "icon")
    val icon: Uri?,

    @ColumnInfo(name = "secret")
    val secret: String,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "issuer")
    val issuer: String,

    @ColumnInfo(name = "algorithm", defaultValue = "0")
    val algorithm: OtpDigest,

    @ColumnInfo(name = "type", defaultValue = "0")
    val type: OtpType,

    @ColumnInfo(name = "digits", defaultValue = "6")
    val digits: Int,

    @ColumnInfo(name = "period", defaultValue = "30")
    val period: Int,
)
