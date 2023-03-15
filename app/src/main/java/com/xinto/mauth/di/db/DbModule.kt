package com.xinto.mauth.di.db

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.xinto.mauth.db.AccountDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Migrate3to4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE countdata (account_id BLOB NOT NULL, count INTEGER NOT NULL, PRIMARY KEY(account_id))")
        database.execSQL("INSERT INTO countdata (account_id, count) SELECT id, counter FROM accounts")
        database.execSQL("CREATE TABLE accounts_temp (id BLOB NOT NULL, icon TEXT, secret TEXT NOT NULL, label TEXT NOT NULL, issuer TEXT NOT NULL, algorithm INTEGER NOT NULL DEFAULT 0, type INTEGER NOT NULL DEFAULT 0, digits INTEGER NOT NULL DEFAULT 6, period INTEGER NOT NULL DEFAULT 30, PRIMARY KEY(id))")
        database.execSQL("INSERT INTO accounts_temp (id, icon, secret, label, issuer, algorithm, type, digits, period) SELECT id, icon, secret, label, issuer, algorithm, type, digits, period FROM accounts")
        database.execSQL("DROP TABLE accounts")
        database.execSQL("ALTER TABLE accounts_temp RENAME TO accounts")
    }
}

val DbModule = module {
   single {
       Room.databaseBuilder(androidContext(), AccountDatabase::class.java, "accounts")
           .addMigrations(Migrate3to4)
           .build()
   }
}