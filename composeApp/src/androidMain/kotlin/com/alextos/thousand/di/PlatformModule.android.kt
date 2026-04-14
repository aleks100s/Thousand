package com.alextos.thousand.di

import com.alextos.thousand.data.ThousandDatabase
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
}
