package com.southboundstudios.tripcheq.data.repository

import android.util.Log
import com.southboundstudios.tripcheq.data.local.dao.FuelRateDao
import com.southboundstudios.tripcheq.data.local.dao.TollDao
import com.southboundstudios.tripcheq.data.local.dao.VehicleDao
import com.southboundstudios.tripcheq.data.remote.dto.FuelRateDto
import com.southboundstudios.tripcheq.data.remote.dto.VehicleDto
import com.southboundstudios.tripcheq.data.remote.dto.ExpresswayDto
import com.southboundstudios.tripcheq.data.remote.dto.TollRateDto
import com.southboundstudios.tripcheq.data.remote.dto.TollAliasDto
import com.southboundstudios.tripcheq.domain.repository.TripRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.flow.Flow

class TripRepositoryImpl(
    private val httpClient: HttpClient,
    private val vehicleDao: VehicleDao,
    private val fuelRateDao: FuelRateDao,
    private val tollDao: TollDao,
) : TripRepository {

    override fun getLocalVehicles() = vehicleDao.getAllVehicles()

    override fun getLocalFuelRates(region: String) = fuelRateDao.getRatesByRegion(region)

    override suspend fun syncData(): Result<Unit> {
        val result = runCatching {
            Log.d("TollDebug", "Starting Supabase Sync...")

            val remoteVehicles: List<VehicleDto> = httpClient.get("vehicles").body()
            val remoteFuelRates: List<FuelRateDto> = httpClient.get("fuel_rates").body()
            val remoteExpressways: List<ExpresswayDto> = httpClient.get("expressways").body()
            val remoteTollAliases: List<TollAliasDto> = httpClient.get("toll_aliases").body()

            val remoteTollRates = mutableListOf<TollRateDto>()
            var offset = 0
            val pageSize = 1000

            while (true) {
                Log.d("TollDebug", "Fetching toll_rates from row $offset to ${offset + pageSize - 1}...")
                val chunk: List<TollRateDto> = httpClient.get("toll_rates") {
                    header("Range-Unit", "items")
                    header("Range", "$offset-${offset + pageSize - 1}")
                }.body()

                remoteTollRates.addAll(chunk)
                Log.d("TollDebug", "Downloaded ${chunk.size} rows in this chunk.")

                if (chunk.size < pageSize) break
                offset += pageSize
            }

            Log.d("TollDebug", "SUCCESS! Total Toll Rates Downloaded: ${remoteTollRates.size}")

            // DEBUG: Let's see if Calamba -> Filinvest actually exists in the downloaded data
            val debugRoute = remoteTollRates.find {
                it.entryName.equals("Calamba", ignoreCase = true) &&
                        it.exitName.equals("Filinvest", ignoreCase = true)
            }
            Log.d("TollDebug", "Debug Check - Calamba to Filinvest: $debugRoute")

            // Map to Entities
            val vehicleEntities = remoteVehicles.map { it.toEntity() }
            val fuelRateEntities = remoteFuelRates.map { it.toEntity() }
            val expresswayEntities = remoteExpressways.map { it.toEntity() }
            val tollRateEntities = remoteTollRates.map { it.toEntity() }
            val tollAliasEntities = remoteTollAliases.map { it.toEntity() }

            // Save to Room
            vehicleDao.insertAll(vehicleEntities)
            fuelRateDao.insertAll(fuelRateEntities)
            tollDao.insertExpressways(expresswayEntities)
            tollDao.insertTollRates(tollRateEntities)
            tollDao.insertTollAliases(tollAliasEntities)

            Log.d("TollDebug", "All data successfully saved to local SQLite Room Database.")
            Log.d("TollDebug", "Downloaded ${remoteExpressways.size} expressways. First operator: ${remoteExpressways.firstOrNull()?.operator}")
            Unit
        }

        result.onFailure { error ->
            Log.e("TollDebug", "CRITICAL SYNC ERROR: ${error.message}", error)
        }

        return result
    }
}