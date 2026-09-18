package com.southboundstudios.tripcheq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expressways")
data class ExpresswayEntity(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val operator: String,
)