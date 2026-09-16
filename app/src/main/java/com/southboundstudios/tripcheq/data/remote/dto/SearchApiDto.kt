package com.southboundstudios.tripcheq.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MapboxGeocodingResponse(
    val features: List<GeocodingFeature>
)

@Serializable
data class GeocodingFeature(
    val id: String,
    val text: String,
    val place_name: String,
    val geometry: GeometryDto,
)

@Serializable
data class GeometryDto(
    val type: String,
    val coordinates: List<Double>, // Mapbox returns [longitude, latitude]
)