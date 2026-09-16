package com.southboundstudios.tripcheq.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.southboundstudios.tripcheq.domain.model.FuelCostParams
import com.southboundstudios.tripcheq.domain.repository.TripRepository
import com.southboundstudios.tripcheq.domain.usecase.CalculateFuelCostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripCalculatorViewModel(
    private val repository: TripRepository,
    private val calculateFuelCost: CalculateFuelCostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState

    init {
        viewModelScope.launch { repository.syncData() }
        viewModelScope.launch {
            repository.getLocalVehicles().collect { vehicles ->
                _uiState.value = _uiState.value.copy(
                    vehicles = vehicles,
                    isLoading = false,
                    // Auto-select a default if empty
                    selectedVehicle = _uiState.value.selectedVehicle ?: vehicles.firstOrNull()
                )
            }
        }
    }

    fun selectVehicle(vehicleId: String) {
        val vehicle = _uiState.value.vehicles.find { it.id == vehicleId }
        _uiState.value = _uiState.value.copy(selectedVehicle = vehicle)
    }

    fun calculateCost(cityDist: Double, hwyDist: Double, fuelPrice: Double) {
        val vehicle = _uiState.value.selectedVehicle ?: return
        val params = FuelCostParams(
            cityDistanceKm = cityDist,
            highwayDistanceKm = hwyDist,
            defaultCityKpl = vehicle.cityKpl,
            defaultHighwayKpl = vehicle.highwayKpl,
            defaultPricePerLiter = fuelPrice
        )
        _uiState.value = _uiState.value.copy(costResult = calculateFuelCost(params))
    }
}