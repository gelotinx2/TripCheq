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
import com.southboundstudios.tripcheq.presentation.TransitRouteResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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

    override suspend fun findTransitRoutes(
        origLng: Double,
        origLat: Double,
        destLng: Double,
        destLat: Double
    ): List<TransitRouteResult> {
        return try {
            val requestJson = buildJsonObject {
                put("orig_lng", origLng)
                put("orig_lat", origLat)
                put("dest_lng", destLng)
                put("dest_lat", destLat)
                put("search_radius", 1500.0)
            }.toString()

            Log.d("TransitDebug", "--- OUTGOING RPC REQUEST ---")
            Log.d("TransitDebug", "Endpoint: rpc/find_transit_routes")
            Log.d("TransitDebug", "Payload: $requestJson")

            val response = httpClient.post("rpc/find_transit_routes") {
                header(io.ktor.http.HttpHeaders.ContentType, io.ktor.http.ContentType.Application.Json.toString())
                setBody(requestJson)
            }

            val rawBody = response.body<String>()
            Log.d("TransitDebug", "--- INCOMING RPC RESPONSE ---")
            Log.d("TransitDebug", "HTTP Status: ${response.status}")
            Log.d("TransitDebug", "Raw Body: $rawBody")

            if (response.status.value in 200..299) {
                val routes = Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<List<TransitRouteResult>>(rawBody)

                Log.d("TransitDebug", "Parsed Routes Count: ${routes.size}")
                routes.forEachIndexed { index, route ->
                    Log.d(
                        "TransitDebug",
                        "[$index] Route: ${route.routeName} | Variant: ${route.variantName} | Signboard: ${route.signboard} | Fare: ₱${route.fare} | Geom Length: ${route.slicedGeometry.length}"
                    )
                }
                routes
            } else {
                Log.e("TransitDebug", "Supabase RPC Error: $rawBody")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TransitDebug", "Exception in findTransitRoutes: ${e.message}", e)
            emptyList()
        }
    }
}