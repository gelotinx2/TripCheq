package com.southboundstudios.tripcheq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.southboundstudios.tripcheq.data.local.dao.FuelRateDao
import com.southboundstudios.tripcheq.data.local.dao.VehicleDao
import com.southboundstudios.tripcheq.data.local.entity.FuelRateEntity
import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity

@Database(
    entities = [VehicleEntity::class, FuelRateEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class TripDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelRateDao(): FuelRateDao
}