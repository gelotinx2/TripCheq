package com.southboundstudios.tripcheq.domain.model

data class FuelCostResult(
    val totalLitersConsumed: Double,
    val totalCostPeso: Double,
    val appliedCityKpl: Double,
    val appliedHighwayKpl: Double,
    val appliedPricePerLiter: Double,
)