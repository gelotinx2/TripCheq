package com.southboundstudios.tripcheq.data.local

import androidx.room.TypeConverter
import com.southboundstudios.tripcheq.domain.model.FuelType

class RoomConverters {
    @TypeConverter
    fun fromFuelType(fuelType: FuelType): String {
        return fuelType.name
    }

    @TypeConverter
    fun toFuelType(value: String): FuelType {
        return FuelType.fromString(value)
    }
}