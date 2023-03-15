package com.xinto.mauth.di.db

import androidx.room.Room
import com.xinto.mauth.db.AccountDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val DbModule = module {
   single {
       Room.databaseBuilder(androidContext(), AccountDatabase::class.java, "accounts")
           .build()
   }
}