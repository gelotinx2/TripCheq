package com.southboundstudios.tripcheq.data.remote.dto

import com.southboundstudios.tripcheq.data.local.entity.FuelRateEntity
import com.southboundstudios.tripcheq.domain.model.FuelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class FuelRateDto(
    val id: String,
    @SerialName("fuel_type") val fuelType: String,
    val region: String,
    @SerialName("price_per_liter") val pricePerLiter: Double,
    @SerialName("updated_at") val updatedAt: String,
) {
    fun toEntity() = FuelRateEntity(
        id = id,
        fuelType = FuelType.fromString(fuelType),
        region = region,
        pricePerLiter = pricePerLiter,
        updatedAt = Instant.parse(updatedAt).toEpochMilli(),
    )
}