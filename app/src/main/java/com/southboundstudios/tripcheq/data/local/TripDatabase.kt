package com.southboundstudios.tripcheq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.southboundstudios.tripcheq.data.local.dao.FuelRateDao
import com.southboundstudios.tripcheq.data.local.dao.TollDao
import com.southboundstudios.tripcheq.data.local.dao.VehicleDao
import com.southboundstudios.tripcheq.data.local.entity.ExpresswayEntity
import com.southboundstudios.tripcheq.data.local.entity.FuelRateEntity
import com.southboundstudios.tripcheq.data.local.entity.TollAliasEntity
import com.southboundstudios.tripcheq.data.local.entity.TollRateEntity
import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        FuelRateEntity::class,
        ExpresswayEntity::class,
        TollRateEntity::class,
        TollAliasEntity::class,
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class TripDatabase : RoomDatabase() {
    abstract val vehicleDao: VehicleDao
    abstract val fuelRateDao: FuelRateDao
    abstract val tollDao: TollDao
}