package com.xinto.mauth.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    companion object Migrations {

        val Migrate3to4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE countdata (account_id BLOB NOT NULL, count INTEGER NOT NULL, PRIMARY KEY(account_id))")
                database.execSQL("INSERT INTO countdata (account_id, count) SELECT id, counter FROM accounts")
                database.execSQL("CREATE TABLE accounts_temp (id BLOB NOT NULL, icon TEXT, secret TEXT NOT NULL, label TEXT NOT NULL, issuer TEXT NOT NULL, algorithm INTEGER NOT NULL DEFAULT 0, type INTEGER NOT NULL DEFAULT 0, digits INTEGER NOT NULL DEFAULT 6, period INTEGER NOT NULL DEFAULT 30, PRIMARY KEY(id))")
                database.execSQL("INSERT INTO accounts_temp (id, icon, secret, label, issuer, algorithm, type, digits, period) SELECT id, icon, secret, label, issuer, algorithm, type, digits, period FROM accounts")
                database.execSQL("DROP TABLE accounts")
                database.execSQL("ALTER TABLE accounts_temp RENAME TO accounts")
            }
        }

        val Migrate4To5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE accounts ADD COLUMN create_date INTEGER NOT NULL DEFAULT 0")
                database.execSQL("UPDATE accounts SET create_date = strftime('%s','now') + ROWID")
            }
        }
    }
}