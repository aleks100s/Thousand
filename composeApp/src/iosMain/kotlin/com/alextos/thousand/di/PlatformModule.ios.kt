package com.alextos.thousand.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.alextos.thousand.data.ThousandDatabase
import com.alextos.thousand.data.service.IosDiceHapticsService
import com.alextos.thousand.data.local.PREFERENCES_DATA_STORE_FILE_NAME
import com.alextos.thousand.data.local.createPreferencesDataStore
import com.alextos.thousand.data.getDatabaseBuilder
import com.alextos.thousand.data.getRoomDatabase
import com.alextos.thousand.domain.service.DiceHapticsService
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<ThousandDatabase> {
        getRoomDatabase(
            builder = getDatabaseBuilder(),
        )
    }
    single<DataStore<Preferences>> {
        createPreferencesDataStore(
            producePath = {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                requireNotNull(documentDirectory?.path) + "/$PREFERENCES_DATA_STORE_FILE_NAME"
            },
        )
    }
    single<DiceHapticsService> {
        IosDiceHapticsService()
    }
}
