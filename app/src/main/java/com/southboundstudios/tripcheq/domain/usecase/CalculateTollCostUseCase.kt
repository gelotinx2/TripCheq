package com.southboundstudios.tripcheq.domain.usecase

import android.util.Log
import com.southboundstudios.tripcheq.data.local.dao.TollDao
import com.southboundstudios.tripcheq.data.local.entity.TollRateEntity
import com.southboundstudios.tripcheq.data.remote.dto.MapboxRouteDto
import com.southboundstudios.tripcheq.data.remote.dto.MapboxIntersectionDto
import com.southboundstudios.tripcheq.domain.util.TollRouteNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TollResult(
    val autosweepCost: Double = 0.0,
    val easytripCost: Double = 0.0,
    val totalCost: Double = 0.0,
)

class CalculateTollCostUseCase(
    private val tollDao: TollDao,
    private val normalizer: TollRouteNormalizer = TollRouteNormalizer()
) {
    suspend operator fun invoke(route: MapboxRouteDto, vehicleClass: Int = 1): TollResult = withContext(Dispatchers.IO) {
        var autosweepTotal = 0.0
        var easytripTotal = 0.0

        val tollBooths = mutableListOf<MapboxIntersectionDto>()

        Log.d("TollDebug", "========== TOLL CALCULATION ENGINE STARTED ==========")

        // --- PHASE 1: EXTRACTION & ALIAS NORMALIZATION ---
        Log.d("TollDebug", "[PHASE 1] Extracting and Normalizing Toll Booths...")
        route.legs.forEach { leg ->
            leg.steps.forEach { step ->
                step.intersections.forEach { intersection ->
                    if (intersection.tollCollection != null) {
                        tollBooths.add(intersection)
                    }
                }
            }
        }

        // Normalize every booth name using toll_aliases right away
        val resolvedBooths = tollBooths.mapNotNull { booth ->
            val mapboxName = booth.tollCollection?.name
            val rawWithSpace = mapboxName ?: "${booth.location[0]}, ${booth.location[1]}"
            val rawNoSpace = mapboxName ?: "${booth.location[0]},${booth.location[1]}"

            var official = tollDao.getOfficialName(rawWithSpace)
            if (official == null) {
                official = tollDao.getOfficialName(rawNoSpace)
            }

            val finalName = official ?: rawWithSpace

            if (finalName == rawWithSpace && finalName.contains(",")) {
                null
            } else {
                finalName
            }
        }.distinct()

        resolvedBooths.forEachIndexed { index, name ->
            Log.d("TollDebug", "   -> Normalized Booth $index: [$name]")
        }

        if (resolvedBooths.isEmpty()) {
            Log.d("TollDebug", "========== ENGINE STOPPED (NO TOLLS) ==========")
            return@withContext TollResult()
        }

        // --- PHASE 2: APPLY METRO MANILA TOLL RULES ---
        Log.d("TollDebug", "[PHASE 2] Applying Toll Route Rules...")
        val billedLegs = normalizer.normalize(resolvedBooths)

        // --- PHASE 3: GRAPH PRE-LOAD ---
        Log.d("TollDebug", "[PHASE 3] Loading Database for Graph Pathfinding...")
        val allRates = tollDao.getAllRates()
        val graph = allRates.groupBy { it.entryName.lowercase() }
        Log.d("TollDebug", "[PHASE 3] Loaded ${allRates.size} rates into Graph Memory.")

        // --- PHASE 4: CALCULATE COSTS FROM NORMALIZED LEGS ---
        Log.d("TollDebug", "[PHASE 4] Calculating Costs for Billed Legs...")
        for (leg in billedLegs) {
            Log.d("TollDebug", "\n  Processing Leg: [${leg.entry}] -> [${leg.exit}] (${leg.note})")

            // 1. Try Direct Database Match
            val directRate = tollDao.getRate(leg.entry, leg.exit)

            if (directRate != null) {
                processPayment(directRate, vehicleClass) { cost, operator ->
                    Log.d("TollDebug", "  => DIRECT MATCH SUCCESS! $operator - ₱$cost")
                    if (operator.equals("Autosweep", ignoreCase = true)) autosweepTotal += cost
                    else if (operator.equals("Easytrip", ignoreCase = true)) easytripTotal += cost
                }
            } else {
                // 2. Try BFS Graph Pathfinding (Handles unmapped system transitions)
                Log.d("TollDebug", "  => No direct match. Initiating BFS Graph Search...")
                val path = findSeamlessPath(leg.entry, leg.exit, graph)

                if (path != null) {
                    Log.d("TollDebug", "  => SEAMLESS GRAPH PATH FOUND! Hops: ${path.size}")
                    path.forEach { hopRate ->
                        processPayment(hopRate, vehicleClass) { cost, operator ->
                            Log.d("TollDebug", "     * Hop Paid: [$operator] ₱$cost for ${hopRate.entryName} -> ${hopRate.exitName}")
                            if (operator.equals("Autosweep", ignoreCase = true)) autosweepTotal += cost
                            else if (operator.equals("Easytrip", ignoreCase = true)) easytripTotal += cost
                        }
                    }
                } else {
                    Log.d("TollDebug", "  => ERROR: Leg [${leg.entry}] -> [${leg.exit}] bypassed (No direct or graph match).")
                }
            }
        }

        Log.d("TollDebug", "========== TOLL CALCULATION ENGINE FINISHED ==========")
        Log.d("TollDebug", "FINAL BREAKDOWN -> Autosweep: ₱$autosweepTotal | Easytrip: ₱$easytripTotal | Total: ₱${autosweepTotal + easytripTotal}")

        return@withContext TollResult(autosweepCost = autosweepTotal, easytripCost = easytripTotal, totalCost = autosweepTotal + easytripTotal)
    }

    private suspend fun processPayment(rate: TollRateEntity, vehicleClass: Int, onPaid: (Double, String) -> Unit) {
        val operator = tollDao.getOperator(rate.expresswayId) ?: "Unknown"
        val cost = when (vehicleClass) {
            2 -> rate.class2Rate
            3 -> rate.class3Rate
            else -> rate.class1Rate
        }
        onPaid(cost, operator)
    }

    private fun findSeamlessPath(entry: String, exit: String, graph: Map<String, List<TollRateEntity>>): List<TollRateEntity>? {
        val queue = ArrayDeque<List<TollRateEntity>>()
        val visited = mutableSetOf<String>()

        val startEdges = graph[entry.lowercase()] ?: return null
        for (edge in startEdges) { queue.add(listOf(edge)) }
        visited.add(entry.lowercase())

        while (queue.isNotEmpty()) {
            val currentPath = queue.removeFirst()
            if (currentPath.size > 4) continue

            val lastNode = currentPath.last().exitName.lowercase()
            if (lastNode == exit.lowercase()) return currentPath

            if (!visited.contains(lastNode)) {
                visited.add(lastNode)
                val nextEdges = graph[lastNode] ?: emptyList()
                for (edge in nextEdges) {
                    val newPath = currentPath.toMutableList().apply { add(edge) }
                    queue.add(newPath)
                }
            }
        }
        return null
    }
}