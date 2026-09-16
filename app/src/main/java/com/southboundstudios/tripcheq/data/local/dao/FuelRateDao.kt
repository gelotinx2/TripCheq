package com.southboundstudios.tripcheq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.southboundstudios.tripcheq.data.local.entity.FuelRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRateDao {
    @Query("SELECT * FROM fuel_rates WHERE region = :region")
    fun getRatesByRegion(region: String): Flow<List<FuelRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<FuelRateEntity>)

    @Query("DELETE FROM fuel_rates")
    suspend fun clearAll()
}