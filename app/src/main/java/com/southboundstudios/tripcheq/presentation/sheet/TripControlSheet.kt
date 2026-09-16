package com.southboundstudios.tripcheq.presentation.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.presentation.TripUiState

@Composable
fun TripControlSheet(
    uiState: TripUiState,
    onOriginQueryChanged: (String) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onOriginSelected: (GeocodingFeature) -> Unit,
    onDestinationSelected: (GeocodingFeature) -> Unit,
    onClearOrigin: () -> Unit,
    onClearDestination: () -> Unit,
    onVehicleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text("Plan Your Trip", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        LocationSearchBar(
            query = uiState.originQuery,
            label = "Origin",
            isLocked = uiState.originCoordinates != null,
            suggestions = uiState.searchSuggestions,
            isShowingSuggestions = uiState.isSearchingOrigin,
            onQueryChanged = onOriginQueryChanged,
            onSuggestionSelected = onOriginSelected,
            onClear = onClearOrigin,
        )

        Spacer(Modifier.height(8.dp))

        LocationSearchBar(
            query = uiState.destinationQuery,
            label = "Destination",
            isLocked = uiState.destinationCoordinates != null,
            suggestions = uiState.searchSuggestions,
            isShowingSuggestions = !uiState.isSearchingOrigin,
            onQueryChanged = onDestinationQueryChanged,
            onSuggestionSelected = onDestinationSelected,
            onClear = onClearDestination,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Vehicle: ${uiState.selectedVehicle?.make} ${uiState.selectedVehicle?.model} - ${uiState.selectedVehicle?.variant ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Estimated Fuel Cost",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₱${uiState.costResult?.totalCostPeso ?: "0.00"}",
                    style = MaterialTheme.typography.headlineLarge,
                )

                if (uiState.costResult != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${uiState.costResult.totalLitersConsumed} Liters consumed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}