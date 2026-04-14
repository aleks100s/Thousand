package com.alextos.thousand.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger

fun initKoin(
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication {
    return startKoin {
        logger(PrintLogger(Level.INFO))
        appDeclaration()
        modules(appModule)
    }
}

fun doInitKoin(): KoinApplication = initKoin()
