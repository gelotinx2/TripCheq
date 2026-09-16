package com.southboundstudios.tripcheq.domain.model

data class FuelCostParams(
    val cityDistanceKm: Double,
    val highwayDistanceKm: Double,
    val defaultCityKpl: Double,
    val defaultHighwayKpl: Double,
    val defaultPricePerLiter: Double,
    // Overrides
    val customCityKpl: Double? = null,
    val customHighwayKpl: Double? = null,
    val customPricePerLiter: Double? = null,
)