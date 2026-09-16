package com.southboundstudios.tripcheq.data.repository

import com.southboundstudios.tripcheq.data.local.dao.FuelRateDao
import com.southboundstudios.tripcheq.data.local.dao.VehicleDao
import com.southboundstudios.tripcheq.data.remote.dto.FuelRateDto
import com.southboundstudios.tripcheq.data.remote.dto.VehicleDto
import com.southboundstudios.tripcheq.domain.repository.TripRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow

class TripRepositoryImpl(
    private val httpClient: HttpClient,
    private val vehicleDao: VehicleDao,
    private val fuelRateDao: FuelRateDao,
) : TripRepository {

    override fun getLocalVehicles() = vehicleDao.getAllVehicles()

    override fun getLocalFuelRates(region: String) = fuelRateDao.getRatesByRegion(region)

    override suspend fun syncData(): Result<Unit> = runCatching {
        val remoteVehicles: List<VehicleDto> = httpClient.get("vehicles").body()
        val remoteFuelRates: List<FuelRateDto> = httpClient.get("fuel_rates").body()
        val vehicleEntities = remoteVehicles.map { it.toEntity() }
        val fuelRateEntities = remoteFuelRates.map { it.toEntity() }

        vehicleDao.insertAll(vehicleEntities)
        fuelRateDao.insertAll(fuelRateEntities)
    }
}