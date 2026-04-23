package com.alextos.thousand.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.alextos.thousand.data.ThousandDatabase
import com.alextos.thousand.data.local.PREFERENCES_DATA_STORE_FILE_NAME
import com.alextos.thousand.data.local.createPreferencesDataStore
import com.alextos.thousand.data.getDatabaseBuilder
import com.alextos.thousand.data.getRoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<ThousandDatabase> {
        getRoomDatabase(
            builder = getDatabaseBuilder(androidContext()),
        )
    }
    single<DataStore<Preferences>> {
        createPreferencesDataStore(
            producePath = {
                androidContext()
                    .filesDir
                    .resolve(PREFERENCES_DATA_STORE_FILE_NAME)
                    .absolutePath
            },
        )
    }
}
