package com.southboundstudios.tripcheq.data.remote.dto

import com.southboundstudios.tripcheq.data.local.entity.ExpresswayEntity
import kotlinx.serialization.Serializable

@Serializable
data class ExpresswayDto(
    val id: String,
    val code: String,
    val name: String,
    val operator: String,
) {
    fun toEntity() = ExpresswayEntity(
        id = id,
        code = code,
        name = name,
        operator = operator,
    )
}