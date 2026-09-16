package com.southboundstudios.tripcheq.data.repository

import com.southboundstudios.tripcheq.BuildConfig
import com.southboundstudios.tripcheq.data.remote.dto.GeocodingFeature
import com.southboundstudios.tripcheq.data.remote.dto.MapboxGeocodingResponse
import com.southboundstudios.tripcheq.domain.repository.SearchRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SearchRepositoryImpl(
    private val httpClient: HttpClient
) : SearchRepository {

    private val MAPBOX_TOKEN = BuildConfig.MAPBOX_TOKEN

    override suspend fun searchPlaces(query: String): Result<List<GeocodingFeature>> = runCatching {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val response: MapboxGeocodingResponse = httpClient.get("https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json") {
            parameter("access_token", MAPBOX_TOKEN)
            parameter("autocomplete", true)
            parameter("country", "ph")
            parameter("types", "poi,address,place,neighborhood")
            parameter("proximity", "121.0000,14.4250")
            parameter("limit", 8)
        }.body()

        response.features
    }
}