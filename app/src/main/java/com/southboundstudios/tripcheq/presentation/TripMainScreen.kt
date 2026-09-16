package com.southboundstudios.tripcheq.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.southboundstudios.tripcheq.presentation.map.TripMapScreen
import com.southboundstudios.tripcheq.presentation.sheet.TripControlSheet
import org.koin.androidx.compose.koinViewModel

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
                onVehicleSelected = { viewModel.selectVehicle(it) },
            )
        },
        sheetPeekHeight = 350.dp,
    ) { innerPadding ->
        TripMapScreen(
            encodedPolyline = uiState.encodedPolyline,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}