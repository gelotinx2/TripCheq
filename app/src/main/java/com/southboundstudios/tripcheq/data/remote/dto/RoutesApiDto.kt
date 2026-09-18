package com.southboundstudios.tripcheq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MapboxDirectionsResponse(
    val routes: List<MapboxRouteDto>
)

@Serializable
data class MapboxRouteDto(
    val distance: Double,
    val duration: Double,
    val geometry: String,
    val legs: List<MapboxLegDto>,
)

@Serializable
data class MapboxLegDto(
    val steps: List<MapboxStepDto>,
    val notifications: List<MapboxNotificationDto>? = emptyList()
)

@Serializable
data class MapboxStepDto(
    val name: String? = null,
    val ref: String? = null,
    val intersections: List<MapboxIntersectionDto>
)

@Serializable
data class MapboxIntersectionDto(
    @SerialName("geometry_index") val geometryIndex: Int,
    val location: List<Double>, // [longitude, latitude]
    @SerialName("toll_collection") val tollCollection: MapboxTollCollectionDto? = null
)

@Serializable
data class MapboxTollCollectionDto(
    val type: String? = null,
    val name: String? = null
)

@Serializable
data class MapboxNotificationDto(
    @SerialName("geometry_index_start") val geometryIndexStart: Int,
    @SerialName("geometry_index_end") val geometryIndexEnd: Int,
    val type: String? = null,
    val subtype: String? = null
)