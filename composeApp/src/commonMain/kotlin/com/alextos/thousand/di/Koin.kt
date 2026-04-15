package com.alextos.thousand.di

import com.alextos.thousand.data.seed.DatabaseSeeder
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger

fun initKoin(
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication {
    val koinApplication = startKoin {
        logger(PrintLogger(Level.INFO))
        appDeclaration()
        modules(appModule)
    }

    if (isDebugBuild) {
        koinApplication.koin.get<DatabaseSeeder>().seedInBackground()
    }

    return koinApplication
}

fun doInitKoin(): KoinApplication = initKoin()
