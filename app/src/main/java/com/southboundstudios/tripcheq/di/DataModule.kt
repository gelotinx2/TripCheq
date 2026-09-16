package com.southboundstudios.tripcheq.di

import androidx.room.Room
import com.southboundstudios.tripcheq.data.local.TripDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            TripDatabase::class.java,
            "tripcheq_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<TripDatabase>().vehicleDao() }
    single { get<TripDatabase>().fuelRateDao() }
}