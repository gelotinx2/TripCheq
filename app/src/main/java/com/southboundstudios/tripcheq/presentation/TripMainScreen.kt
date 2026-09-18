package com.southboundstudios.tripcheq.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.presentation.map.TripMapScreen
import com.southboundstudios.tripcheq.presentation.sheet.TripControlSheet
import org.koin.androidx.compose.koinViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMainScreen(
    viewModel: TripCalculatorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BottomSheetScaffold(
        sheetContent = {
            TripControlSheet(
                uiState = uiState,
                onOriginQueryChanged = { viewModel.onSearchQueryChanged(it, isOrigin = true) },
                onDestinationQueryChanged = { viewModel.onSearchQueryChanged(it, isOrigin = false) },
                onOriginSelected = { viewModel.onPlaceSelected(it, isOrigin = true) },
                onDestinationSelected = { viewModel.onPlaceSelected(it, isOrigin = false) },
                onClearOrigin = { viewModel.clearOrigin() },
                onClearDestination = { viewModel.clearDestination() },
                onVehicleSelected = { viewModel.selectVehicle(it) },
                onSelectCustomVehicle = { viewModel.selectCustomVehicle() },
                onCustomCityKplChanged = { viewModel.onCustomCityKplChanged(it) },
                onCustomHighwayKplChanged = { viewModel.onCustomHighwayKplChanged(it) },
                onCalculateClicked = { viewModel.calculateTrip() },
            )
        },
        sheetPeekHeight = 360.dp,
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
                            DividerDefaults.Thickness,
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