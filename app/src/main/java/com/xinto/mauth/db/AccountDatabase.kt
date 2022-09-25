package com.xinto.mauth.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xinto.mauth.db.converter.OtpConverter
import com.xinto.mauth.db.converter.UuidConverter
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount

@Database(
    entities = [EntityAccount::class],
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2
        )
    ]
)
@TypeConverters(UuidConverter::class, OtpConverter::class)
abstract class AccountDatabase : RoomDatabase() {

    abstract fun accountsDao(): AccountsDao

}