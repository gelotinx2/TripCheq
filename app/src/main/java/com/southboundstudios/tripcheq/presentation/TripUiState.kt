package com.southboundstudios.tripcheq.presentation

import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity
import com.southboundstudios.tripcheq.domain.model.FuelCostResult

data class TripUiState(
    val isLoading: Boolean = true,
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicle: VehicleEntity? = null,
    val origin: String = "",
    val destination: String = "",
    val costResult: FuelCostResult? = null,
)