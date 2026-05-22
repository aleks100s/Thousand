package com.alextos.thousand.di

import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger

fun initKoin(
    shakeDeviceObserver: ShakeDeviceObserver,
    nativeAccountService: NativeAccountService,
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication {
    val koinApplication = startKoin {
        logger(PrintLogger(Level.INFO))
        appDeclaration()
        modules(appModule(shakeDeviceObserver, nativeAccountService))
    }

    return koinApplication
}

fun doInitKoin(
    shakeDeviceObserver: ShakeDeviceObserver,
    nativeAccountService: NativeAccountService
): KoinApplication = initKoin(shakeDeviceObserver, nativeAccountService)
