package com.southboundstudios.tripcheq.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.domain.model.FuelCostParams
import com.southboundstudios.tripcheq.domain.repository.RouteRepository
import com.southboundstudios.tripcheq.domain.repository.SearchRepository
import com.southboundstudios.tripcheq.domain.repository.TripRepository
import com.southboundstudios.tripcheq.domain.usecase.CalculateFuelCostUseCase
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
            defaultPricePerLiter = fuelPrice,
        )
        _uiState.value = _uiState.value.copy(costResult = calculateFuelCost(params))
    }

    fun onSearchQueryChanged(query: String, isOrigin: Boolean) {
        _uiState.value = if (isOrigin) {
            _uiState.value.copy(originQuery = query, isSearchingOrigin = true)
        } else {
            _uiState.value.copy(destinationQuery = query, isSearchingOrigin = false)
        }

        searchJob?.cancel()

        if (query.length < 3) {
            _uiState.value = _uiState.value.copy(searchSuggestions = emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            delay(timeMillis = 500)

            searchRepository.searchPlaces(query).onSuccess { results ->
                _uiState.value = _uiState.value.copy(searchSuggestions = results)
            }
        }
    }

    fun onPlaceSelected(
        feature: GeocodingFeature,
        isOrigin: Boolean,
    ) {
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

        val state = _uiState.value
        if (state.originCoordinates != null && state.destinationCoordinates != null) {
            calculateTrip(
                originLat = state.originCoordinates.first,
                originLng = state.originCoordinates.second,
                destLat = state.destinationCoordinates.first,
                destLng = state.destinationCoordinates.second,
            )
        }
    }

    fun calculateTrip(
        originLat: Double, originLng: Double,
        destLat: Double, destLng: Double,
    ) {
        viewModelScope.launch {
            val result = routeRepository.fetchRoute(originLat, originLng, destLat, destLng)

            result.onSuccess { response ->
                val route = response.routes.firstOrNull()

                if (route != null) {
                    val totalDistanceKm = route.distance / 1000.0
                    val estimatedCityKm = totalDistanceKm * 0.40
                    val estimatedHighwayKm = totalDistanceKm * 0.60
                    val currentFuelPrice = 60.00
                    val costResult = calculateFuelCost(
                        FuelCostParams(
                            cityDistanceKm = estimatedCityKm,
                            highwayDistanceKm = estimatedHighwayKm,
                            defaultCityKpl = _uiState.value.selectedVehicle?.cityKpl ?: 10.0,
                            defaultHighwayKpl = _uiState.value.selectedVehicle?.highwayKpl ?: 15.0,
                            defaultPricePerLiter = currentFuelPrice
                        )
                    )

                    _uiState.value = _uiState.value.copy(
                        encodedPolyline = route.geometry,
                        costResult = costResult
                    )
                }
            }.onFailure { error ->
                // Handle API error state here
            }
        }
    }
}