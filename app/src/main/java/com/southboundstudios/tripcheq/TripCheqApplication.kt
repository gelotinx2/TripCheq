package com.southboundstudios.tripcheq

import android.app.Application
import com.southboundstudios.tripcheq.di.dataModule
import com.southboundstudios.tripcheq.di.networkModule
import com.southboundstudios.tripcheq.di.domainModule
import com.southboundstudios.tripcheq.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.maplibre.android.MapLibre

class TripCheqApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MapLibre.getInstance(this)

        startKoin {
            androidLogger()
            androidContext(this@TripCheqApplication)
            modules(dataModule, networkModule, domainModule, presentationModule)
        }
    }
}