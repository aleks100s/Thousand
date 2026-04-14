package com.alextos.thousand

import android.app.Application
import com.alextos.thousand.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ThousandApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@ThousandApplication)
        }
    }
}
