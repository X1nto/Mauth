package com.xinto.mauth.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xinto.mauth.db.converter.OtpConverter
import com.xinto.mauth.db.converter.UriConverter
import com.xinto.mauth.db.converter.UuidConverter
import com.xinto.mauth.db.dao.account.AccountsDao
import com.xinto.mauth.db.dao.account.entity.EntityAccount
import com.xinto.mauth.db.dao.rtdata.RtdataDao
import com.xinto.mauth.db.dao.rtdata.entity.EntityCountData

@Database(
    entities = [EntityAccount::class, EntityCountData::class],
    version = 5,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2
        ),
        AutoMigration(
            from = 2,
            to = 3
        ),
        AutoMigration(
            from = 4,
            to = 5
        )
    ]
)
@TypeConverters(UuidConverter::class, OtpConverter::class, UriConverter::class)
abstract class AccountDatabase : RoomDatabase() {

    abstract fun accountsDao(): AccountsDao

    abstract fun rtdataDao(): RtdataDao

}