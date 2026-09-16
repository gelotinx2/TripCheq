package com.southboundstudios.tripcheq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.southboundstudios.tripcheq.domain.model.FuelType

@Entity(tableName = "fuel_rates")
data class FuelRateEntity(
    @PrimaryKey val id: String,
    val fuelType: FuelType,
    val region: String,
    val pricePerLiter: Double,
    val updatedAt: Long
)