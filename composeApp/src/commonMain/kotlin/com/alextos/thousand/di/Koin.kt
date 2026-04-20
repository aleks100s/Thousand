package com.alextos.thousand.di

import com.alextos.thousand.data.seed.DatabaseSeeder
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger

fun initKoin(
    shakeDeviceObserver: ShakeDeviceObserver,
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication {
    val koinApplication = startKoin {
        logger(PrintLogger(Level.INFO))
        appDeclaration()
        modules(appModule(shakeDeviceObserver))
    }

    if (isDebugBuild) {
        koinApplication.koin.get<DatabaseSeeder>().seedInBackground()
    }

    return koinApplication
}

fun doInitKoin(shakeDeviceObserver: ShakeDeviceObserver): KoinApplication = initKoin(shakeDeviceObserver)
