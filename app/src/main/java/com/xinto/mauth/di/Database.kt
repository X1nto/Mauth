package com.xinto.mauth.di

import android.content.Context
import androidx.room.Room
import com.xinto.mauth.db.AccountDatabase
import com.xinto.mauth.db.dao.AccountsDao
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dbModule = module {

    fun provideDatabase(context: Context): AccountDatabase {
        return Room.databaseBuilder(context, AccountDatabase::class.java, "accounts")
            .build()
    }

    fun provideAccountsDao(accountDatabase: AccountDatabase): AccountsDao {
        return accountDatabase.accountsDao()
    }

    singleOf(::provideDatabase)
    singleOf(::provideAccountsDao)
}