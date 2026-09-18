package com.southboundstudios.tripcheq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "toll_rates")
data class TollRateEntity(
    @PrimaryKey val id: String,
    val expresswayId: String,
    val entryName: String,
    val exitName: String,
    val class1Rate: Double,
    val class2Rate: Double,
    val class3Rate: Double
)