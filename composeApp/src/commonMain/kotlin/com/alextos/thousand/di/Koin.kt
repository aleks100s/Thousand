package com.alextos.thousand.di

import com.alextos.thousand.domain.service.GamePresenceObserver
import com.alextos.thousand.domain.service.NativeAccountService
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.repository.MultiplayerRepository
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger

fun initKoin(
    shakeDeviceObserver: ShakeDeviceObserver,
    gamePresenceObserver: GamePresenceObserver,
    nativeAccountService: NativeAccountService,
    multiplayerRepository: MultiplayerRepository,
    appDeclaration: KoinApplication.() -> Unit = {},
): KoinApplication {
    val koinApplication = startKoin {
        logger(PrintLogger(Level.INFO))
        appDeclaration()
        modules(appModule(shakeDeviceObserver, gamePresenceObserver, nativeAccountService, multiplayerRepository))
    }

    return koinApplication
}

fun doInitKoin(
    shakeDeviceObserver: ShakeDeviceObserver,
    gamePresenceObserver: GamePresenceObserver,
    nativeAccountService: NativeAccountService,
    multiplayerRepository: MultiplayerRepository,
): KoinApplication = initKoin(shakeDeviceObserver, gamePresenceObserver, nativeAccountService, multiplayerRepository)
