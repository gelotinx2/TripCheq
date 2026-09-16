package com.southboundstudios.tripcheq.presentation

import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.domain.model.FuelCostResult

data class TripUiState(
    val isLoading: Boolean = true,
    val isCalculating: Boolean = false,
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicle: VehicleEntity? = null,
    val isCustomVehicle: Boolean = false,
    val customCityKpl: String = "",
    val customHighwayKpl: String = "",
    val originQuery: String = "",
    val originCoordinates: Pair<Double, Double>? = null,
    val destinationQuery: String = "",
    val destinationCoordinates: Pair<Double, Double>? = null,
    val searchSuggestions: List<GeocodingFeature> = emptyList(),
    val isSearchingOrigin: Boolean = false,
    val encodedPolyline: String? = null,
    val routeDistanceKm: Double? = null,
    val costResult: FuelCostResult? = null,
    val errorMessage: String? = null,
) {
    val canCalculate: Boolean
        get() {
            val hasCoords = originCoordinates != null && destinationCoordinates != null
            val hasValidVehicle = if (isCustomVehicle) {
                (customCityKpl.toDoubleOrNull() ?: 0.0) > 0.0 &&
                        (customHighwayKpl.toDoubleOrNull() ?: 0.0) > 0.0
            } else {
                selectedVehicle != null
            }
            return hasCoords && hasValidVehicle && !isCalculating
        }
}