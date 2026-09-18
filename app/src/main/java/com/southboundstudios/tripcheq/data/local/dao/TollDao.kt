package com.southboundstudios.tripcheq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.southboundstudios.tripcheq.data.local.entity.ExpresswayEntity
import com.southboundstudios.tripcheq.data.local.entity.TollAliasEntity
import com.southboundstudios.tripcheq.data.local.entity.TollRateEntity

@Dao
interface TollDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpressways(expressways: List<ExpresswayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTollRates(rates: List<TollRateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTollAliases(aliases: List<TollAliasEntity>)

    // Edge Case 1 & 2: Translate Mapbox strings/coordinates to official TRB names
    @Query("SELECT officialName FROM toll_aliases WHERE LOWER(mapboxName) = LOWER(:input) LIMIT 1")
    suspend fun getOfficialName(input: String): String?

    // Edge Case 4: The Fallback Lookup (Using roadKeyword to prevent NLEX/SLEX collisions)
    @Query("""
        SELECT * FROM toll_rates 
        WHERE LOWER(entryName) = LOWER(:entryName) 
        AND LOWER(exitName) = LOWER(:exitName) 
        LIMIT 1
    """)
    suspend fun getRate(entryName: String, exitName: String): TollRateEntity?

    @Query("SELECT * FROM toll_rates")
    suspend fun getAllRates(): List<TollRateEntity>

    @Query("SELECT operator FROM expressways WHERE id = :expresswayId")
    suspend fun getOperator(expresswayId: String): String?
}