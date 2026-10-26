package com.alextos.thousand

import android.app.Application
import com.alextos.thousand.data.repository.AndroidMultiplayerRepository
import com.alextos.thousand.data.service.AndroidAccountService
import com.alextos.thousand.data.service.AndroidGamePresenceObserver
import com.alextos.thousand.data.service.AndroidShakeDeviceObserver
import com.alextos.thousand.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ThousandApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val shakeDeviceObserver = AndroidShakeDeviceObserver(this).apply {
            start()
        }
        val gamePresenceObserver = AndroidGamePresenceObserver(this)

        val accountService = AndroidAccountService(this)
        initKoin(
            shakeDeviceObserver = shakeDeviceObserver,
            gamePresenceObserver = gamePresenceObserver,
            nativeAccountService = accountService,
            multiplayerRepository = AndroidMultiplayerRepository(accountService),
        ) {
            androidLogger()
            androidContext(this@ThousandApplication)
        }
    }
}
