package com.southboundstudios.tripcheq.domain.repository

import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature

interface SearchRepository {
    suspend fun searchPlaces(query: String): Result<List<GeocodingFeature>>
}