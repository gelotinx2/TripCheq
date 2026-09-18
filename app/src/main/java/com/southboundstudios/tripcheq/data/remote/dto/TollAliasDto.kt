package com.southboundstudios.tripcheq.data.remote.dto

import com.southboundstudios.tripcheq.data.local.entity.TollAliasEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TollAliasDto(
    val id: String,
    @SerialName("mapbox_name")
    val mapboxName: String, // Catches "Philippine Arena" or "121.014,14.521"
    @SerialName("official_name")
    val officialName: String,  // Maps to "Ciudad de Victoria"
) {
    fun toEntity() = TollAliasEntity(
        id = id,
        mapboxName = mapboxName,
        officialName = officialName,
    )
}