package com.southboundstudios.tripcheq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MapboxDirectionsResponse(
    val routes: List<MapboxRouteDto>
)

@Serializable
data class MapboxRouteDto(
    val distance: Double, // returned in meters
    val duration: Double, // returned in seconds
    val geometry: String,  // the encoded polyline string
)