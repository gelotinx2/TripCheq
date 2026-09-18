package com.southboundstudios.tripcheq.data.repository

import com.southboundstudios.tripcheq.BuildConfig.MAPBOX_TOKEN
import com.southboundstudios.tripcheq.data.remote.dto.MapboxDirectionsResponse
import com.southboundstudios.tripcheq.domain.repository.RouteRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RouteRepositoryImpl(
    private val httpClient: HttpClient
) : RouteRepository {
    override suspend fun fetchRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        avoidTolls: Boolean,
    ): Result<MapboxDirectionsResponse> = runCatching {

        // Mapbox Format: {longitude},{latitude};{longitude},{latitude}
        val coordinates = "$originLng,$originLat;$destLng,$destLat"

        httpClient.get("https://api.mapbox.com/directions/v5/mapbox/driving/$coordinates") {
            parameter("access_token", MAPBOX_TOKEN)
            parameter("geometries", "polyline")
            parameter("overview", "full")
            parameter("steps", "true")
            if (avoidTolls) {
                parameter("exclude", "toll")
            }
        }.body()
    }
}