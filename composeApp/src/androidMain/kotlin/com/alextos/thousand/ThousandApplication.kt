package com.alextos.thousand

import android.app.Application
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

        initKoin(shakeDeviceObserver) {
            androidLogger()
            androidContext(this@ThousandApplication)
        }
    }
}
