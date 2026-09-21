package com.southboundstudios.tripcheq.domain.util

import android.util.Log
import com.southboundstudios.tripcheq.domain.model.BilledLeg

class TollRouteNormalizer {

    private val nlexOpenExits = setOf("valenzuela", "karuhatan", "lingunan", "meycauayan", "marilao", "ciudad de victoria", "philippine arena")
    private val nlexFlat85Exits = setOf("valenzuela", "karuhatan", "lingunan", "meycauayan")
    private val nlexOpenEntries = setOf("marilao", "meycauayan", "lingunan", "karuhatan", "valenzuela", "ciudad de victoria", "philippine arena")

    private val sky3Intermediates = setOf("del monte", "quezon avenue", "e. rodriguez", "aurora", "n. domingo", "plaza dilao", "nagtahan", "quirino", "plaza azul")
    private val slexBooths = setOf("sucat", "bicutan", "alabang", "filinvest", "susana heights", "san pedro", "southwoods", "carmona", "mamplasan", "sta. rosa", "abi", "cabuyao", "silangan", "calamba", "hillsborough")

    fun normalize(officialBooths: List<String>): List<BilledLeg> {
        val legs = mutableListOf<BilledLeg>()
        if (officialBooths.isEmpty()) return legs

        println("--- Normalizer Started with ${officialBooths.size} Booths ---")

        val hasDelMonte = officialBooths.any { it.equals("Del Monte", ignoreCase = true) }
        val delMonteIndex = officialBooths.indexOfFirst { it.equals("Del Monte", ignoreCase = true) }

        var currentEntry = officialBooths.first()
        var i = 1

        while (i < officialBooths.size) {
            val currentBooth = officialBooths[i]
            val currentBoothLower = currentBooth.lowercase()
            var currentEntryLower = currentEntry.lowercase()
            val isLastBooth = (i == officialBooths.size - 1)

            // --- RULE CATEGORY A & B: SLEX / SKYWAY ---
            if (currentBoothLower.contains("hillsborough")) {
                legs.add(BilledLeg(entry = currentEntry, exit = "Skyway", note = "Rule A1: SLEX to Elevated"))

                currentEntry = if (hasDelMonte && i < delMonteIndex) {
                    "Buendia"
                } else {
                    "Skyway"
                }
                i++
                continue
            }

            // --- RULE CATEGORY C4: SKY3 to SLEX (Southbound Transition) ---
            if ((currentEntryLower.contains("balintawak") || currentEntryLower == "nlex") && slexBooths.any { currentBoothLower.contains(it) }) {
                legs.add(BilledLeg(entry = currentEntry, exit = "Buendia", note = "Rule C4: Skyway Stage 3 Southbound Completion"))
                currentEntry = "Skyway"
                currentEntryLower = "skyway"
            }

            // Rule B3: Skyway Stage 3 Intermediate Markers
            if (sky3Intermediates.any { currentBoothLower.contains(it) }) {
                // FIX: If it is the last booth, do NOT bypass it. Close the leg.
                if (isLastBooth) {
                    var finalExit = currentBooth
                    // Map the physical Del Monte barrier to the Quezon Avenue exit from your matrix
                    if (finalExit.equals("del monte", ignoreCase = true)) {
                        finalExit = "Quezon Avenue"
                    }
                    legs.add(BilledLeg(entry = currentEntry, exit = finalExit, note = "Final Route Leg (Sky3 Exit)"))
                    break
                }

                if (sky3Intermediates.any { currentEntryLower.contains(it) } || currentEntryLower.contains("balintawak")) {
                    currentEntry = "NLEX"
                }
                println("[Rule B3] Skyway Stage 3 intermediate bypassed: $currentBooth")
                i++
                continue
            }

            // Rule A3: SLEX Intermediate Bypasses
            if (currentEntryLower == "skyway" && !isLastBooth) {
                if (!currentBoothLower.contains("hillsborough")) {
                    println("[Rule A3] Bypassing intermediate SLEX gate: $currentBooth")
                    i++
                    continue
                }
            }

            // --- RULE CATEGORY C: SYSTEM TRANSITIONS ---
            if (currentBoothLower.contains("balintawak-a.bonifacio") ||
                currentBoothLower.contains("balintawak toll plaza a") ||
                currentBoothLower.contains("a.bonifacio-balintawak")) {

                legs.add(BilledLeg(entry = currentEntry, exit = "A.Bonifacio-Balintawak", note = "Rule C1: Skyway Stage 3 Northbound Completion"))

                if (i + 1 < officialBooths.size) {
                    currentEntry = "Balintawak"
                }
                i++
                continue
            }

            // Rule C3: SCTEX to TPLEX (Northbound)
            if (currentBoothLower == "tarlac" || currentBoothLower.contains("tarlac city toll plaza")) {
                if (currentEntryLower == "tarlac") {
                    i++
                    continue
                } else {
                    legs.add(BilledLeg(entry = currentEntry, exit = "Tarlac", note = "Rule C3: SCTEX Northbound Completion"))
                    if (i + 1 < officialBooths.size) {
                        currentEntry = "La Paz"
                    }
                    i++
                    continue
                }
            }

            // Rule C3: TPLEX to SCTEX (Southbound)
            if (currentBoothLower == "la paz" || currentBoothLower.contains("la paz toll plaza")) {
                if (currentEntryLower == "la paz") {
                    i++
                    continue
                } else {
                    legs.add(BilledLeg(entry = currentEntry, exit = "La Paz", note = "Rule C3: TPLEX Southbound Completion"))
                    if (i + 1 < officialBooths.size) {
                        currentEntry = "Tarlac"
                    }
                    i++
                    continue
                }
            }

            // --- RULE CATEGORY E: NLEX OPEN / CLOSED SYSTEMS ---
            if (currentBoothLower.contains("bocaue") && currentEntryLower !in listOf("balintawak", "mindanao avenue", "nlex")) {
                legs.add(BilledLeg(entry = currentEntry, exit = "Balintawak", note = "Rule E3: NLEX Southbound Consolidation"))

                while (i + 1 < officialBooths.size && officialBooths[i + 1].lowercase() in nlexOpenExits) {
                    i++
                }

                if (i + 1 < officialBooths.size) {
                    val nextBooth = officialBooths[i + 1].lowercase()
                    // FIX: If transitioning from NLEX to Skyway 3, use "NLEX" as the entry to perfectly match the matrix
                    if (sky3Intermediates.any { nextBooth.contains(it) } || nextBooth.contains("balintawak")) {
                        currentEntry = "NLEX"
                    } else {
                        currentEntry = officialBooths[i + 1]
                    }
                } else {
                    i++
                    continue
                }
            }

            if (currentEntryLower in listOf("balintawak", "mindanao avenue") && !isLastBooth) {
                if (currentBoothLower in nlexOpenExits || currentBoothLower.contains("bocaue")) {
                    i++
                    continue
                }
            }

            // --- FINAL BOOTH CLOSURE ---
            if (isLastBooth) {
                var finalExit = currentBooth

                if (currentEntryLower in listOf("balintawak", "mindanao avenue") && currentBoothLower in nlexOpenExits) {
                    if (currentBoothLower in nlexFlat85Exits) {
                        finalExit = "Marilao"
                    }
                }

                if (currentEntryLower in nlexOpenEntries && currentBoothLower.contains("balintawak")) {
                    if (currentEntryLower in nlexFlat85Exits) {
                        currentEntry = "Marilao"
                    }
                }

                legs.add(BilledLeg(entry = currentEntry, exit = finalExit, note = "Final Route Leg"))
                break
            }

            i++
        }

        if (officialBooths.size == 1) {
            legs.add(BilledLeg(entry = currentEntry, exit = currentEntry, note = "Rule D1: Flat Rate / Single Pass"))
        }

        return legs
    }
}