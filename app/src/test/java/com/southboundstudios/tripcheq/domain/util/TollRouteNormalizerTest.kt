package com.southboundstudios.tripcheq.domain.util

import com.southboundstudios.tripcheq.domain.model.BilledLeg
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TollRouteNormalizerTest {

    private lateinit var normalizer: TollRouteNormalizer

    @Before
    fun setup() {
        normalizer = TollRouteNormalizer()
    }

    @Test
    fun `route from Dau to Quezon Avenue generates NLEX consolidation and Sky3 exit`() {
        // Arrange: The messy Mapbox sequence we saw in the logs
        val mapboxBooths = listOf("Dau", "Bocaue", "Del Monte")

        // Act
        val result = normalizer.normalize(mapboxBooths)

        // Assert
        assertEquals(2, result.size)

        // Leg 1: NLEX Southbound Consolidation
        assertEquals("Dau", result[0].entry)
        assertEquals("Balintawak", result[0].exit)

        // Leg 2: Skyway 3 Southbound
        assertEquals("NLEX", result[1].entry)
        assertEquals("Quezon Avenue", result[1].exit) // Mapped from Del Monte
    }

    @Test
    fun `route from Tanauan to Rosario generates STAR, SLEX, Sky3, SCTEX, and TPLEX legs`() {
        // Arrange
        val mapboxBooths = listOf(
            "Tanauan", "Hillsborough Toll Plaza", "Del Monte",
            "Balintawak-A.Bonifacio", "Tarlac City Toll Plaza",
            "Tarlac Central Toll Plaza", "Rosario"
        )

        // Act
        val result = normalizer.normalize(mapboxBooths)

        // Assert
        assertEquals(4, result.size)
        assertEquals("Tanauan", result[0].entry)
        assertEquals("Skyway", result[0].exit) // SLEX

        assertEquals("Buendia", result[1].entry)
        assertEquals("A.Bonifacio-Balintawak", result[1].exit) // Sky3

        assertEquals("Balintawak", result[2].entry)
        assertEquals("Tarlac", result[2].exit) // NLEX/SCTEX

        assertEquals("La Paz", result[3].entry)
        assertEquals("Rosario", result[3].exit) // TPLEX
    }
}