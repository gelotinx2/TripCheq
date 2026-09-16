package com.southboundstudios.tripcheq.presentation.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.presentation.TripUiState

@Composable
fun TripControlSheet(
    uiState: TripUiState,
    onVehicleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Plan Your Trip", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.origin,
            onValueChange = { /* Update state */ },
            label = { Text("Origin") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.destination,
            onValueChange = { /* Update state */ },
            label = { Text("Destination") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Vehicle: ${uiState.selectedVehicle?.make} ${uiState.selectedVehicle?.model} - ${uiState.selectedVehicle?.variant ?: ""}")

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Estimated Fuel Cost", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "₱${uiState.costResult?.totalCostPeso ?: "0.00"}",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}