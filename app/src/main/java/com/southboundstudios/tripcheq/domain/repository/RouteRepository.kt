package com.southboundstudios.tripcheq.domain.repository

import com.southboundstudios.tripcheq.data.remote.dto.MapboxDirectionsResponse

interface RouteRepository {
    suspend fun fetchRoute(
        originLat: Double, originLng: Double,
        destLat: Double, destLng: Double,
    ): Result<MapboxDirectionsResponse>
}