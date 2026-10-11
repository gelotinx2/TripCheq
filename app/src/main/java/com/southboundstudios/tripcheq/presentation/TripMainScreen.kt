package com.southboundstudios.tripcheq.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.presentation.map.TripMapScreen
import com.southboundstudios.tripcheq.presentation.sheet.TripControlSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMainScreen(
    viewModel: TripCalculatorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 56.dp,
        sheetContent = {
            TripControlSheet(
                uiState = uiState,
                onModeChanged = { viewModel.setTripMode(it) },
                onOriginQueryChanged = { viewModel.onSearchQueryChanged(it, isOrigin = true) },
                onDestinationQueryChanged = { viewModel.onSearchQueryChanged(it, isOrigin = false) },
                onOriginSelected = { viewModel.onPlaceSelected(it, isOrigin = true) },
                onDestinationSelected = { viewModel.onPlaceSelected(it, isOrigin = false) },
                onClearOrigin = { viewModel.clearOrigin() },
                onClearDestination = { viewModel.clearDestination() },
                onSwapLocations = { viewModel.swapLocations() },
                onVehicleSelected = { viewModel.selectVehicle(it) },
                onSelectCustomVehicle = { viewModel.selectCustomVehicle() },
                onCustomCityKplChanged = { viewModel.onCustomCityKplChanged(it) },
                onCustomHighwayKplChanged = { viewModel.onCustomHighwayKplChanged(it) },
                onDiscountToggled = { viewModel.onDiscountToggled(it) },
                onPrioritySelected = { viewModel.onPrioritySelected(it) },
                onCalculateClicked = {
                    viewModel.calculateTrip()
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TripMapScreen(
                encodedPolyline = uiState.encodedPolyline,
                modifier = Modifier.fillMaxSize()
            )

            val showResultCard = (uiState.selectedMode == TripMode.DRIVE && uiState.costResult != null) ||
                    (uiState.selectedMode == TripMode.COMMUTE && uiState.selectedTransitRoute != null)

            AnimatedVisibility(
                visible = showResultCard,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (uiState.selectedMode == TripMode.DRIVE) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "₱${String.format("%.2f", uiState.grandTotal)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Cost Breakdown",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Fuel", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "₱${String.format("%.2f", uiState.costResult?.totalCostPeso ?: 0.0)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (uiState.totalTollCost > 0) {
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Toll Fees", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "₱${String.format("%.2f", uiState.totalTollCost)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (uiState.autosweepCost > 0 || uiState.easytripCost > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        val rfids = listOfNotNull(
                                            if (uiState.autosweepCost > 0) "Autosweep: ₱${String.format("%.2f", uiState.autosweepCost)}" else null,
                                            if (uiState.easytripCost > 0) "Easytrip: ₱${String.format("%.2f", uiState.easytripCost)}" else null
                                        ).joinToString(" • ")

                                        Text(
                                            text = rfids,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Distance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${String.format("%.1f", uiState.routeDistanceKm ?: 0.0)} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            if (uiState.transitResults.isNotEmpty()) {
                                // 1. Horizontal Variant Selection Chips
                                if (uiState.transitResults.size > 1) {
                                    Text(
                                        text = "Route Variants",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        uiState.transitResults.forEach { result ->
                                            val isSelected = result == uiState.selectedTransitRoute
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.selectTransitRoute(result) },
                                                label = { Text(result.variantName) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.DirectionsBus,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(Modifier.height(10.dp))
                                }

                                // 2. Route & Signboard Info
                                uiState.selectedTransitRoute?.let { route ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = route.routeName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = route.variantName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = route.signboard,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // 3. Fare Display
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Fare", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(
                                            "₱${String.format("%.2f", uiState.activeCommuteFare)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}