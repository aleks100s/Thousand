package com.alextos.thousand

import android.app.Application
import com.alextos.thousand.data.repository.MultiplayerRepositoryImpl
import com.alextos.thousand.data.service.AndroidAccountService
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

        initKoin(
            shakeDeviceObserver = shakeDeviceObserver,
            nativeAccountService = AndroidAccountService(this),
            multiplayerRepository = MultiplayerRepositoryImpl(),
        ) {
            androidLogger()
            androidContext(this@ThousandApplication)
        }
    }
}
