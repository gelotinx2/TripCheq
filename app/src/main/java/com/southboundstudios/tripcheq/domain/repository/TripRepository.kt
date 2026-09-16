package com.southboundstudios.tripcheq.domain.repository

import com.southboundstudios.tripcheq.data.local.entity.FuelRateEntity
import com.southboundstudios.tripcheq.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getLocalVehicles(): Flow<List<VehicleEntity>>
    fun getLocalFuelRates(region: String): Flow<List<FuelRateEntity>>
    suspend fun syncData(): Result<Unit>
}