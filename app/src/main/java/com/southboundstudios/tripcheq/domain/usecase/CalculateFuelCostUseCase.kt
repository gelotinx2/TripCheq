package com.southboundstudios.tripcheq.domain.usecase

import com.southboundstudios.tripcheq.domain.model.FuelCostParams
import com.southboundstudios.tripcheq.domain.model.FuelCostResult
import kotlin.math.round

class CalculateFuelCostUseCase {

    operator fun invoke(params: FuelCostParams): FuelCostResult {
        val activeCityKpl = params.customCityKpl ?: params.defaultCityKpl
        val activeHighwayKpl = params.customHighwayKpl ?: params.defaultHighwayKpl
        val activePricePerLiter = params.customPricePerLiter ?: params.defaultPricePerLiter

        if (activeCityKpl <= 0.0 || activeHighwayKpl <= 0.0) {
            return FuelCostResult(0.0, 0.0, activeCityKpl, activeHighwayKpl, activePricePerLiter)
        }

        val cityLiters = params.cityDistanceKm / activeCityKpl
        val highwayLiters = params.highwayDistanceKm / activeHighwayKpl
        val totalLiters = cityLiters + highwayLiters

        val rawTotalCost = totalLiters * activePricePerLiter

        return FuelCostResult(
            totalLitersConsumed = totalLiters.roundTo(2),
            totalCostPeso = rawTotalCost.roundTo(2),
            appliedCityKpl = activeCityKpl,
            appliedHighwayKpl = activeHighwayKpl,
            appliedPricePerLiter = activePricePerLiter
        )
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }
}