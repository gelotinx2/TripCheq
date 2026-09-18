package com.southboundstudios.tripcheq.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "toll_aliases")
data class TollAliasEntity(
    @PrimaryKey val id: String,
    val mapboxName: String,
    val officialName: String
)