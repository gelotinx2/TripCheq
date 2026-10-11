package com.southboundstudios.tripcheq.presentation

import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.domain.model.FuelCostResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class TripMode {
    DRIVE, COMMUTE
}

enum class CommutePriority {
    FASTEST, LEAST_TRANSFERS
}

data class CommutePreferences(
    val priority: CommutePriority = CommutePriority.FASTEST,
    val isDiscounted: Boolean = false,
)

@Serializable
data class TransitRouteResult(
    @SerialName("route_name") val routeName: String,
    @SerialName("variant_name") val variantName: String,
    @SerialName("signboard") val signboard: String,
    @SerialName("fare") val fare: Double,
    @SerialName("sliced_geometry") val slicedGeometry: String,
)

data class TripUiState(
    val selectedMode: TripMode = TripMode.DRIVE,
    val isLoading: Boolean = true,
    val isCalculating: Boolean = false,

    // Drive-specific
    val vehicles: List<VehicleEntity> = emptyList(),
    val selectedVehicle: VehicleEntity? = null,
    val isCustomVehicle: Boolean = false,
    val customCityKpl: String = "",
    val customHighwayKpl: String = "",
    val costResult: FuelCostResult? = null,
    val autosweepCost: Double = 0.0,
    val easytripCost: Double = 0.0,
    val totalTollCost: Double = 0.0,

    // Commute-specific
    val commutePreferences: CommutePreferences = CommutePreferences(),
    val transitResults: List<TransitRouteResult> = emptyList(),
    val selectedTransitRoute: TransitRouteResult? = null,

    // Shared Search & Geometry
    val originQuery: String = "",
    val originCoordinates: Pair<Double, Double>? = null, // Pair(lat, lng)
    val destinationQuery: String = "",
    val destinationCoordinates: Pair<Double, Double>? = null, // Pair(lat, lng)
    val searchSuggestions: List<GeocodingFeature> = emptyList(),
    val isSearchingOrigin: Boolean = false,
    val encodedPolyline: String? = null,
    val routeDistanceKm: Double? = null,
    val errorMessage: String? = null,
) {
    val canCalculate: Boolean
        get() {
            val hasCoords = originCoordinates != null && destinationCoordinates != null
            if (!hasCoords || isCalculating) return false

            return when (selectedMode) {
                TripMode.DRIVE -> {
                    if (isCustomVehicle) {
                        (customCityKpl.toDoubleOrNull() ?: 0.0) > 0.0 &&
                                (customHighwayKpl.toDoubleOrNull() ?: 0.0) > 0.0
                    } else {
                        selectedVehicle != null
                    }
                }
                TripMode.COMMUTE -> true
            }
        }

    val grandTotal: Double
        get() = (costResult?.totalCostPeso ?: 0.0) + totalTollCost

    val activeCommuteFare: Double
        get() {
            val base = selectedTransitRoute?.fare ?: 0.0
            return if (commutePreferences.isDiscounted) base * 0.80 else base
        }
}