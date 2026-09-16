package com.southboundstudios.tripcheq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.southboundstudios.tripcheq.domain.model.FuelType

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val make: String,
    val model: String,
    val variant: String?,
    val yearStart: Int,
    val yearEnd: Int?,
    val fuelType: FuelType,
    val cityKpl: Double,
    val highwayKpl: Double,
    val isVerified: Boolean,
)