package com.southboundstudios.tripcheq.data.repository

import com.southboundstudios.tripcheq.BuildConfig.MAPBOX_TOKEN
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

    override suspend fun searchPlaces(query: String): Result<List<GeocodingFeature>> = runCatching {
        val response: MapboxGeocodingResponse = httpClient.get("https://api.mapbox.com/geocoding/v5/mapbox.places/$query.json") {
            parameter("access_token", MAPBOX_TOKEN)
            parameter("autocomplete", true)
            parameter("country", "ph")
            parameter("proximity", "120.99,14.44")
            parameter("types", "address,poi,neighborhood,locality,place")
            parameter("limit", 5)
        }.body()

        response.features
    }
}