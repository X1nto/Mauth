package com.xinto.mauth.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val Context.preferences by preferencesDataStore("preferences")

val datastoreModule = module {
    single {
        androidContext().preferences
    }
}