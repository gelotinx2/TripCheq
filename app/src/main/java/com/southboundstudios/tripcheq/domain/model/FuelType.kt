package com.southboundstudios.tripcheq.domain.model

enum class FuelType(val displayName: String) {
    GASOLINE_91("Gasoline 91"),
    GASOLINE_95("Gasoline 95"),
    DIESEL("Diesel");

    companion object {
        fun fromString(value: String): FuelType {
            return entries.find { it.name == value.uppercase() } ?: GASOLINE_91
        }
    }
}