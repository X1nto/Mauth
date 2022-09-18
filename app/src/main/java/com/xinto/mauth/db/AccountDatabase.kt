package com.xinto.mauth.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xinto.mauth.db.converter.UuidConverter
import com.xinto.mauth.db.dao.AccountsDao
import com.xinto.mauth.db.entity.EntityAccount

@Database(entities = [EntityAccount::class], version = 1)
@TypeConverters(UuidConverter::class)
abstract class AccountDatabase : RoomDatabase() {

    abstract fun accountsDao(): AccountsDao

}