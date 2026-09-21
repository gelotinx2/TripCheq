package com.southboundstudios.tripcheq.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
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

            AnimatedVisibility(
                visible = uiState.costResult != null,
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (uiState.costResult != null && uiState.routeDistanceKm != null) {
                            val formattedDistance = String.format("%.1f", uiState.routeDistanceKm)
                            Text(
                                text = "$formattedDistance km • ${uiState.costResult!!.totalLitersConsumed} Liters",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Fuel", style = MaterialTheme.typography.bodyLarge)
                            Text("₱${uiState.costResult?.totalCostPeso ?: "0.00"}", style = MaterialTheme.typography.bodyLarge)
                        }

                        if (uiState.totalTollCost > 0.0) {
                            Spacer(Modifier.height(4.dp))
                            if (uiState.autosweepCost > 0.0) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Autosweep RFID", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                    Text("₱${String.format("%.2f", uiState.autosweepCost)}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (uiState.easytripCost > 0.0) {
                                Spacer(Modifier.height(2.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Easytrip RFID", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("₱${String.format("%.2f", uiState.easytripCost)}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(
                            Modifier,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("₱${String.format("%.2f", uiState.grandTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}