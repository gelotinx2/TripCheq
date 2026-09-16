package com.southboundstudios.tripcheq.data.remote.dto

import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity
import com.southboundstudios.tripcheq.domain.model.FuelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleDto(
    val id: String,
    val make: String,
    val model: String,
    val variant: String? = null,
    @SerialName("year_start")
    val yearStart: Int,
    @SerialName("year_end")
    val yearEnd: Int? = null,
    @SerialName("fuel_type")
    val fuelType: String,
    @SerialName("city_kpl")
    val cityKpl: Double,
    @SerialName("highway_kpl")
    val highwayKpl: Double,
    @SerialName("is_verified")
    val isVerified: Boolean,
) {
    fun toEntity() = VehicleEntity(
        id = id,
        make = make,
        model = model,
        variant = variant,
        yearStart = yearStart,
        yearEnd = yearEnd,
        fuelType = FuelType.fromString(fuelType),
        cityKpl = cityKpl,
        highwayKpl = highwayKpl,
        isVerified = isVerified,
    )
}