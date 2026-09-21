package com.southboundstudios.tripcheq.domain.usecase

import android.util.Log
import com.southboundstudios.tripcheq.data.local.dao.TollDao
import com.southboundstudios.tripcheq.data.local.entity.TollRateEntity
import com.southboundstudios.tripcheq.data.remote.dto.MapboxIntersectionDto
import com.southboundstudios.tripcheq.data.remote.dto.MapboxLegDto
import com.southboundstudios.tripcheq.data.remote.dto.MapboxRouteDto
import com.southboundstudios.tripcheq.data.remote.dto.MapboxStepDto
import com.southboundstudios.tripcheq.data.remote.dto.MapboxTollCollectionDto
import com.southboundstudios.tripcheq.domain.util.TollRouteNormalizer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateTollCostUseCaseTest {

    private lateinit var tollDao: TollDao
    private lateinit var normalizer: TollRouteNormalizer
    private lateinit var useCase: CalculateTollCostUseCase

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        tollDao = mockk() // Create a fake Dao
        normalizer = TollRouteNormalizer() // Use real normalizer
        useCase = CalculateTollCostUseCase(tollDao, normalizer)
    }

    @Test
    fun `direct match calculates correct Easytrip and Autosweep totals`() = runTest {
        // 1. Setup Mock Mapbox Route (Helper function needed to build DTO)
        val mockRoute = createMockRouteWithBooths(listOf("Dau", "Bocaue", "Del Monte"))

        // 2. Instruct the Mock Dao how to behave
        coEvery { tollDao.getOfficialName(any()) } answers { firstArg() } // Return name as-is
        coEvery { tollDao.getAllRates() } returns emptyList() // No graph routing for this test

        // Mock NLEX Leg (Easytrip)
        coEvery { tollDao.getRate("Dau", "Balintawak") } returns TollRateEntity(
            id = "1", expresswayId = "nlex", entryName = "Dau", exitName = "Balintawak",
            class1Rate = 411.0, class2Rate = 0.0, class3Rate = 0.0
        )
        coEvery { tollDao.getOperator("nlex") } returns "Easytrip"

        // Mock Sky3 Leg (Autosweep)
        coEvery { tollDao.getRate("NLEX", "Quezon Avenue") } returns TollRateEntity(
            id = "2", expresswayId = "sky3", entryName = "NLEX", exitName = "Quezon Avenue",
            class1Rate = 129.0, class2Rate = 0.0, class3Rate = 0.0
        )
        coEvery { tollDao.getOperator("sky3") } returns "Autosweep"

        // 3. Execute
        val result = useCase(mockRoute, vehicleClass = 1)

        // 4. Verify
        assertEquals(411.0, result.easytripCost, 0.0)
        assertEquals(129.0, result.autosweepCost, 0.0)
        assertEquals(540.0, result.totalCost, 0.0)
    }
}

private fun createMockRouteWithBooths(boothNames: List<String>): MapboxRouteDto {
    val mockIntersections = boothNames.mapIndexed { index, name ->
        MapboxIntersectionDto(
            geometryIndex = index,
            location = listOf(121.0, 14.5), // [longitude, latitude]
            tollCollection = MapboxTollCollectionDto(
                type = "toll_booth",
                name = name,
            )
        )
    }

    val mockStep = MapboxStepDto(
        name = "Expressway",
        ref = "E1",
        intersections = mockIntersections,
    )

    val mockLeg = MapboxLegDto(
        steps = listOf(mockStep),
        notifications = emptyList(),
    )

    return MapboxRouteDto(
        distance = 0.0,
        duration = 0.0,
        geometry = "dummy_geometry_string",
        legs = listOf(mockLeg),
    )
}