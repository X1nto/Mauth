package com.xinto.mauth.di.db

import com.xinto.mauth.db.AccountDatabase
import org.koin.dsl.module

val DbAccountModule = module {
    single {
        val db: AccountDatabase = get()
        db.accountsDao()
    }
}