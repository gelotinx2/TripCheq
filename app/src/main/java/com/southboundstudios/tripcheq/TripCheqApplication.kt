package com.southboundstudios.tripcheq

import android.app.Application
import com.southboundstudios.tripcheq.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TripCheqApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TripCheqApplication)
            modules(dataModule)
        }
    }
}