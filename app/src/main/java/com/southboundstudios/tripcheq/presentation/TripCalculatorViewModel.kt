package com.southboundstudios.tripcheq.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.domain.model.FuelCostParams
import com.southboundstudios.tripcheq.domain.repository.RouteRepository
import com.southboundstudios.tripcheq.domain.repository.SearchRepository
import com.southboundstudios.tripcheq.domain.repository.TripRepository
import com.southboundstudios.tripcheq.domain.usecase.CalculateFuelCostUseCase
import com.southboundstudios.tripcheq.domain.usecase.CalculateTollCostUseCase
import com.southboundstudios.tripcheq.domain.usecase.TollResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripCalculatorViewModel(
    private val tripRepository: TripRepository,
    private val routeRepository: RouteRepository,
    private val searchRepository: SearchRepository,
    private val calculateFuelCost: CalculateFuelCostUseCase,
    private val calculateTollCost: CalculateTollCostUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState

    private var searchJob: Job? = null

    init {
        viewModelScope.launch { tripRepository.syncData() }
        viewModelScope.launch {
            tripRepository.getLocalVehicles().collect { vehicles ->
                _uiState.value = _uiState.value.copy(
                    vehicles = vehicles,
                    isLoading = false,
                    selectedVehicle = _uiState.value.selectedVehicle ?: vehicles.firstOrNull(),
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String, isOrigin: Boolean) {
        _uiState.value = if (isOrigin) {
            _uiState.value.copy(originQuery = query, isSearchingOrigin = true)
        } else {
            _uiState.value.copy(destinationQuery = query, isSearchingOrigin = false)
        }

        searchJob?.cancel()
        if (query.trim().length < 3) {
            _uiState.value = _uiState.value.copy(searchSuggestions = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(timeMillis = 400)
            searchRepository.searchPlaces(query).onSuccess { results ->
                _uiState.value = _uiState.value.copy(searchSuggestions = results)
            }
        }
    }

    fun onPlaceSelected(feature: GeocodingFeature, isOrigin: Boolean) {
        val lng = feature.geometry.coordinates[0]
        val lat = feature.geometry.coordinates[1]

        _uiState.value = if (isOrigin) {
            _uiState.value.copy(
                originQuery = feature.place_name,
                originCoordinates = Pair(lat, lng),
                searchSuggestions = emptyList(),
            )
        } else {
            _uiState.value.copy(
                destinationQuery = feature.place_name,
                destinationCoordinates = Pair(lat, lng),
                searchSuggestions = emptyList(),
            )
        }
    }

    fun clearOrigin() {
        _uiState.value = _uiState.value.copy(
            originQuery = "",
            originCoordinates = null,
            searchSuggestions = emptyList(),
            encodedPolyline = null,
            routeDistanceKm = null,
            costResult = null
        )
    }

    fun clearDestination() {
        _uiState.value = _uiState.value.copy(
            destinationQuery = "",
            destinationCoordinates = null,
            searchSuggestions = emptyList(),
            encodedPolyline = null,
            routeDistanceKm = null,
            costResult = null
        )
    }

    fun selectVehicle(vehicleId: String) {
        val vehicle = _uiState.value.vehicles.find { it.id == vehicleId }
        _uiState.value = _uiState.value.copy(
            selectedVehicle = vehicle,
            isCustomVehicle = false,
        )
    }

    fun selectCustomVehicle() {
        _uiState.value = _uiState.value.copy(
            selectedVehicle = null,
            isCustomVehicle = true,
        )
    }

    fun onCustomCityKplChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(customCityKpl = value)
        }
    }

    fun onCustomHighwayKplChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(customHighwayKpl = value)
        }
    }

    fun swapLocations() {
        val currentOriginQuery = _uiState.value.originQuery
        val currentOriginCoords = _uiState.value.originCoordinates

        _uiState.value = _uiState.value.copy(
            originQuery = _uiState.value.destinationQuery,
            originCoordinates = _uiState.value.destinationCoordinates,
            destinationQuery = currentOriginQuery,
            destinationCoordinates = currentOriginCoords,
            encodedPolyline = null,
            routeDistanceKm = null,
            costResult = null,
        )
    }

    fun calculateTrip() {
        val state = _uiState.value
        val origin = state.originCoordinates ?: return
        val dest = state.destinationCoordinates ?: return

        _uiState.value = _uiState.value.copy(isCalculating = true, errorMessage = null)

        viewModelScope.launch {
            val isMotorcycle = state.selectedVehicle?.type?.equals("Motorcycle", ignoreCase = true) == true

            val routeResult = routeRepository.fetchRoute(
                originLat = origin.first,
                originLng = origin.second,
                destLat = dest.first,
                destLng = dest.second,
                avoidTolls = isMotorcycle,
            )

            routeResult.onSuccess { response ->
                val route = response.routes.firstOrNull()
                if (route == null) {
                    _uiState.value = _uiState.value.copy(
                        isCalculating = false,
                        errorMessage = "No route found.",
                    )
                    return@launch
                }

                val totalKm = route.distance / 1000.0
                val cityDist = totalKm * 0.45
                val hwyDist = totalKm * 0.55
                val fuelPrice = 80.00

                val (cityKpl, hwyKpl) = if (state.isCustomVehicle) {
                    Pair(
                        state.customCityKpl.toDoubleOrNull() ?: 10.0,
                        state.customHighwayKpl.toDoubleOrNull() ?: 15.0
                    )
                } else {
                    Pair(
                        state.selectedVehicle?.cityKpl ?: 10.0,
                        state.selectedVehicle?.highwayKpl ?: 15.0
                    )
                }

                val cost = calculateFuelCost(
                    FuelCostParams(
                        cityDistanceKm = cityDist,
                        highwayDistanceKm = hwyDist,
                        defaultCityKpl = cityKpl,
                        defaultHighwayKpl = hwyKpl,
                        defaultPricePerLiter = fuelPrice,
                    )
                )

                val tollResult = if (!isMotorcycle) {
                    calculateTollCost(route, vehicleClass = 1)
                } else {
                    TollResult()
                }

                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    encodedPolyline = route.geometry,
                    routeDistanceKm = totalKm,
                    costResult = cost,
                    autosweepCost = tollResult.autosweepCost,
                    easytripCost = tollResult.easytripCost,
                    totalTollCost = tollResult.totalCost,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    errorMessage = error.localizedMessage ?: "Failed to calculate route.",
                )
            }
        }
    }
}