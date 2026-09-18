package com.southboundstudios.tripcheq.data.remote.dto

import com.southboundstudios.tripcheq.data.local.entity.TollRateEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TollRateDto(
    val id: String,
    @SerialName("expressway_id")
    val expresswayId: String,
    @SerialName("entry_name")
    val entryName: String,
    @SerialName("exit_name")
    val exitName: String,
    @SerialName("class_1_rate")
    val class1Rate: Double,
    @SerialName("class_2_rate")
    val class2Rate: Double,
    @SerialName("class_3_rate")
    val class3Rate: Double,
) {
    fun toEntity() = TollRateEntity(
        id = id,
        expresswayId = expresswayId,
        entryName = entryName,
        exitName = exitName,
        class1Rate = class1Rate,
        class2Rate = class2Rate,
        class3Rate = class3Rate,
    )
}