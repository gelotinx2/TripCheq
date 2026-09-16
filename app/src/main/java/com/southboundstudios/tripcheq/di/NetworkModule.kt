package com.southboundstudios.tripcheq.di

import com.southboundstudios.tripcheq.data.repository.RouteRepositoryImpl
import com.southboundstudios.tripcheq.data.repository.SearchRepositoryImpl
import com.southboundstudios.tripcheq.data.repository.TripRepositoryImpl
import com.southboundstudios.tripcheq.domain.repository.RouteRepository
import com.southboundstudios.tripcheq.domain.repository.SearchRepository
import com.southboundstudios.tripcheq.domain.repository.TripRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule = module {

    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            defaultRequest {
                // TODO: In production, move these to BuildConfig / local.properties
                url("https://axwhexgxslvnzrewvqvj.supabase.co/rest/v1/")
                header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF4d2hleGd4c2x2bnpyZXd2cXZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODk1MjYyMTAsImV4cCI6MjEwNTEwMjIxMH0.OwCsJdIlXW6c6VZ9exMDK4xFAAC1_LpUCQsmI-dusHo")
                header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF4d2hleGd4c2x2bnpyZXd2cXZqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODk1MjYyMTAsImV4cCI6MjEwNTEwMjIxMH0.OwCsJdIlXW6c6VZ9exMDK4xFAAC1_LpUCQsmI-dusHo")
            }
        }
    }

    single<TripRepository> {
        TripRepositoryImpl(
            httpClient = get(),
            vehicleDao = get(),
            fuelRateDao = get(),
        )
    }

    single<RouteRepository> {
        RouteRepositoryImpl(
            httpClient = get(),
        )
    }

    single<SearchRepository> {
        SearchRepositoryImpl(
            httpClient = get(),
        )
    }
}