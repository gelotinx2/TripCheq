package com.southboundstudios.tripcheq.presentation

import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.domain.model.FuelCostResult

data class TripUiState(
    val isLoading: Boolean = true,
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicle: VehicleEntity? = null,
    val origin: String = "",
    val destination: String = "",
    val encodedPolyline: String? = null,
    val costResult: FuelCostResult? = null,
    val originQuery: String = "",
    val originCoordinates: Pair<Double, Double>? = null,
    val destinationQuery: String = "",
    val destinationCoordinates: Pair<Double, Double>? = null,
    val searchSuggestions: List<GeocodingFeature> = emptyList(),
    val isSearchingOrigin: Boolean = false,
)