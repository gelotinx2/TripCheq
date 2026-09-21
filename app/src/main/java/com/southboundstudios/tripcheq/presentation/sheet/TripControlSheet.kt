package com.southboundstudios.tripcheq.presentation.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.presentation.TripUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripControlSheet(
    uiState: TripUiState,
    onOriginQueryChanged: (String) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onOriginSelected: (GeocodingFeature) -> Unit,
    onDestinationSelected: (GeocodingFeature) -> Unit,
    onClearOrigin: () -> Unit,
    onClearDestination: () -> Unit,
    onSwapLocations: () -> Unit, // NEW PARAMETER
    onVehicleSelected: (String) -> Unit,
    onSelectCustomVehicle: () -> Unit,
    onCustomCityKplChanged: (String) -> Unit,
    onCustomHighwayKplChanged: (String) -> Unit,
    onCalculateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var vehicleMenuExpanded by remember { mutableStateOf(false) }
    var showCustomVehiclePrompt by remember { mutableStateOf(false) } // MODAL STATE
    val scrollState = rememberScrollState()

    if (showCustomVehiclePrompt) {
        AlertDialog(
            onDismissRequest = { showCustomVehiclePrompt = false },
            title = { Text("Use Custom Vehicle?") },
            text = { Text("Do you want to switch to a custom vehicle to manually edit the fuel efficiency (km/L)?") },
            confirmButton = {
                TextButton(onClick = {
                    onSelectCustomVehicle()
                    showCustomVehiclePrompt = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomVehiclePrompt = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .imePadding() // Pushes content above keyboard
            .verticalScroll(scrollState), // Allows scrolling when keyboard is open
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

        // SWAP BUTTON
        Box(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSwapLocations) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap Locations", tint = MaterialTheme.colorScheme.primary)
            }
        }

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

        Spacer(Modifier.height(14.dp))

        val vehicleLabel = when {
            uiState.isCustomVehicle -> "Custom Vehicle (Manual km/L)"
            uiState.selectedVehicle != null -> {
                val v = uiState.selectedVehicle
                "${v.make} ${v.model} ${v.variant ?: ""}".trim()
            }
            else -> "Select a Vehicle"
        }

        ExposedDropdownMenuBox(
            expanded = vehicleMenuExpanded,
            onExpandedChange = { vehicleMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = vehicleLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Vehicle") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = vehicleMenuExpanded,
                onDismissRequest = { vehicleMenuExpanded = false },
            ) {
                uiState.vehicles.forEach { vehicle ->
                    DropdownMenuItem(
                        text = { Text("${vehicle.make} ${vehicle.model} ${vehicle.variant ?: ""}".trim()) },
                        onClick = {
                            onVehicleSelected(vehicle.id)
                            vehicleMenuExpanded = false
                        },
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Custom Vehicle...") },
                    onClick = {
                        onSelectCustomVehicle()
                        vehicleMenuExpanded = false
                    },
                )
            }
        }

        val displayCityKpl = if (uiState.isCustomVehicle) uiState.customCityKpl else uiState.selectedVehicle?.cityKpl?.toString() ?: ""
        val displayHighwayKpl = if (uiState.isCustomVehicle) uiState.customHighwayKpl else uiState.selectedVehicle?.highwayKpl?.toString() ?: ""

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // WRAPPED IN BOX TO INTERCEPT TAPS
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = displayCityKpl,
                    onValueChange = onCustomCityKplChanged,
                    label = { Text("City km/L") },
                    readOnly = !uiState.isCustomVehicle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (!uiState.isCustomVehicle) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = if (!uiState.isCustomVehicle) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                    ),
                )
                if (!uiState.isCustomVehicle) {
                    Box(modifier = Modifier.matchParentSize().clickable { showCustomVehiclePrompt = true })
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = displayHighwayKpl,
                    onValueChange = onCustomHighwayKplChanged,
                    label = { Text("Hwy km/L") },
                    readOnly = !uiState.isCustomVehicle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (!uiState.isCustomVehicle) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = if (!uiState.isCustomVehicle) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
                    ),
                )
                if (!uiState.isCustomVehicle) {
                    Box(modifier = Modifier.matchParentSize().clickable { showCustomVehiclePrompt = true })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCalculateClicked,
            enabled = uiState.canCalculate,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState.isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Calculate Route")
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}