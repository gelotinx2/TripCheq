package com.southboundstudios.tripcheq.domain.util

import android.util.Log
import com.southboundstudios.tripcheq.domain.model.BilledLeg

class TollRouteNormalizer {

    private val nlexOpenExits = setOf("valenzuela", "karuhatan", "lingunan", "meycauayan", "marilao", "ciudad de victoria", "philippine arena")
    private val nlexFlat85Exits = setOf("valenzuela", "karuhatan", "lingunan", "meycauayan")
    private val nlexOpenEntries = setOf("marilao", "meycauayan", "lingunan", "karuhatan", "valenzuela", "ciudad de victoria", "philippine arena")

    private val sky3Intermediates = setOf("del monte", "quezon avenue", "e. rodriguez", "aurora", "n. domingo", "plaza dilao", "nagtahan", "quirino", "plaza azul")
    private val slexBooths = setOf("sucat", "bicutan", "alabang", "filinvest", "susana heights", "san pedro", "southwoods", "carmona", "mamplasan", "sta. rosa", "abi", "cabuyao", "silangan", "calamba", "hillsborough")

    private val cavitexMainline = setOf("roxas blvd.", "zapote", "kawit", "sucat road /dr. a santos avenue", "c5 road extension/c.p. garcia", "macapagal")
    private val cavitexSouthLink = setOf("merville", "taguig")

    private fun getCavitexStandaloneExit(booth: String): String {
        val lower = booth.lowercase()
        return when {
            lower.contains("roxas blvd") || lower.contains("macapagal") -> "Zapote"
            lower.contains("kawit") -> "Zapote"
            lower.contains("zapote") -> "Roxas Blvd."
            lower.contains("sucat road") -> "Roxas Blvd."
            lower.contains("c5 road") -> "Roxas Blvd."
            lower.contains("taguig") -> "Merville"
            lower.contains("merville") -> "Taguig"
            else -> booth
        }
    }

    private fun resolveCavitexLeg(entry: String, exit: String): BilledLeg {
        var finalEntry = entry
        var finalExit = exit

        // Map Mapbox aliases directly to official matrix boundaries
        if (finalEntry.lowercase().contains("zapote") || finalEntry.lowercase().contains("macapagal")) finalEntry = "Roxas Blvd."
        if (finalExit.lowercase().contains("zapote") || finalExit.lowercase().contains("macapagal")) finalExit = "Roxas Blvd."

        if (finalEntry.lowercase() == finalExit.lowercase()) {
            finalExit = getCavitexStandaloneExit(finalEntry)
        }
        return BilledLeg(entry = finalEntry, exit = finalExit, note = "Rule D4: CAVITEX Resolved Leg")
    }

    fun normalize(officialBooths: List<String>): List<BilledLeg> {
        val legs = mutableListOf<BilledLeg>()
        if (officialBooths.isEmpty()) return legs

        Log.d("TollNormalizer", "--- Normalizer Started with ${officialBooths.size} Booths ---")

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
                currentEntry = if (hasDelMonte && i < delMonteIndex) "Buendia" else "Skyway"
                i++
                continue
            }

            if ((currentEntryLower.contains("balintawak") || currentEntryLower == "nlex") && slexBooths.any { currentBoothLower.contains(it) }) {
                legs.add(BilledLeg(entry = currentEntry, exit = "Buendia", note = "Rule C4: Skyway Stage 3 Southbound Completion"))
                currentEntry = "Skyway"
                currentEntryLower = "skyway"
            }

            // --- RULE CATEGORY B3: SKYWAY STAGE 3 INTERMEDIATE MARKERS ---
            if (sky3Intermediates.any { currentBoothLower.contains(it) }) {

                val isEntryCavitex = cavitexMainline.any { currentEntryLower.contains(it) } || cavitexSouthLink.any { currentEntryLower.contains(it) }
                if (isEntryCavitex) {
                    legs.add(resolveCavitexLeg(currentEntry, officialBooths[i - 1]))
                    currentEntry = "Buendia"
                    currentEntryLower = "buendia"
                } else if (currentEntryLower in nlexOpenEntries && !currentEntryLower.contains("balintawak")) {
                    legs.add(BilledLeg(entry = currentEntry, exit = "Balintawak", note = "Rule C5: NLEX to Sky3 Southbound Transition"))
                    currentEntry = "NLEX"
                    currentEntryLower = "nlex"
                }

                if (isLastBooth) {
                    var finalExit = currentBooth
                    if (finalExit.contains("quezon avenue", ignoreCase = true) || finalExit.equals("del monte", ignoreCase = true)) {
                        finalExit = "Quezon Avenue"
                    } else if (finalExit.contains("e. rodriguez", ignoreCase = true)) {
                        finalExit = "E. Rodriguez"
                    } else if (finalExit.contains("plaza azul", ignoreCase = true) || finalExit.contains("nagtahan", ignoreCase = true)) {
                        finalExit = "Plaza Azul/Nagtahan"
                    }
                    legs.add(BilledLeg(entry = currentEntry, exit = finalExit, note = "Final Route Leg (Sky3 Exit)"))
                    break
                }

                if (currentEntryLower.contains("balintawak")) {
                    currentEntry = "NLEX"
                }
                i++
                continue
            }

            if (currentEntryLower == "skyway" && !isLastBooth) {
                if (!currentBoothLower.contains("hillsborough")) {
                    i++
                    continue
                }
            }

            // --- RULE CATEGORY D: NAIAX & CAVITEX BOUNDARIES ---
            if (currentBoothLower.contains("naiax")) {
                val isEntryCavitex = cavitexMainline.any { currentEntryLower.contains(it) } || cavitexSouthLink.any { currentEntryLower.contains(it) }
                val isEntryNorth = currentEntryLower in listOf("nlex", "balintawak", "skyway", "a.bonifacio-balintawak") || sky3Intermediates.any { currentEntryLower.contains(it) }

                if (isEntryCavitex) {
                    legs.add(resolveCavitexLeg(currentEntry, officialBooths[i - 1]))
                } else if (isEntryNorth) {
                    // Close Skyway leg at Buendia before entering NAIAX
                    legs.add(BilledLeg(entry = currentEntry, exit = "Buendia", note = "Rule C4: Skyway to NAIAX Transition"))
                } else if (!currentEntryLower.contains("naiax")) {
                    legs.add(BilledLeg(entry = currentEntry, exit = currentBooth, note = "Transition to NAIAX"))
                }

                // Use actual booth name to exactly match the DB (e.g. "NAIAX Main Toll Plaza")
                legs.add(BilledLeg(entry = currentBooth, exit = currentBooth, note = "Rule D1: NAIAX Flat Rate"))

                if (i + 1 < officialBooths.size) currentEntry = officialBooths[i + 1]
                i++
                continue
            }

            if (cavitexMainline.any { currentBoothLower.contains(it) }) {
                if (cavitexSouthLink.any { currentEntryLower.contains(it) }) {
                    val exit = if (currentEntryLower.contains("taguig")) "Merville" else "Taguig"
                    legs.add(BilledLeg(entry = currentEntry, exit = exit, note = "Rule D2: C5 South Link Completion"))
                    currentEntry = currentBooth
                } else if (i + 1 < officialBooths.size) {
                    val nextBooth = officialBooths[i + 1].lowercase()
                    if (cavitexSouthLink.any { nextBooth.contains(it) }) {
                        legs.add(BilledLeg(entry = currentEntry, exit = currentBooth, note = "Rule D2: CAVITEX Mainline Completion"))
                        currentEntry = if (nextBooth.contains("taguig")) "Merville" else "Taguig"
                        i++
                        continue
                    }
                }
            }

            // --- RULE CATEGORY C: SYSTEM TRANSITIONS ---
            if (currentBoothLower.contains("balintawak-a.bonifacio") || currentBoothLower.contains("balintawak toll plaza a") || currentBoothLower.contains("a.bonifacio-balintawak")) {

                val isEntryCavitex = cavitexMainline.any { currentEntryLower.contains(it) } || cavitexSouthLink.any { currentEntryLower.contains(it) }
                if (isEntryCavitex) {
                    legs.add(resolveCavitexLeg(currentEntry, officialBooths[i - 1]))
                    currentEntry = "Buendia"
                }

                legs.add(BilledLeg(entry = currentEntry, exit = "A.Bonifacio-Balintawak", note = "Rule C1: Skyway Stage 3 Northbound Completion"))

                if (isLastBooth) {
                    legs.add(BilledLeg(entry = "Balintawak", exit = "Marilao", note = "Rule E1: Implicit NLEX Open System Flat Rate"))
                    break
                } else {
                    currentEntry = "Balintawak"
                }

                i++
                continue
            }

            if (currentBoothLower == "tarlac" || currentBoothLower.contains("tarlac city toll plaza")) {
                if (currentEntryLower == "tarlac") {
                    i++
                    continue
                } else {
                    legs.add(BilledLeg(entry = currentEntry, exit = "Tarlac", note = "Rule C3: SCTEX Northbound Completion"))
                    if (i + 1 < officialBooths.size) currentEntry = "La Paz"
                    i++
                    continue
                }
            }

            if (currentBoothLower == "la paz" || currentBoothLower.contains("la paz toll plaza")) {
                if (currentEntryLower == "la paz") {
                    i++
                    continue
                } else {
                    legs.add(BilledLeg(entry = currentEntry, exit = "La Paz", note = "Rule C3: TPLEX Southbound Completion"))
                    if (i + 1 < officialBooths.size) currentEntry = "Tarlac"
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
                var finalEntry = currentEntry

                val finalEntryLower = finalEntry.lowercase()

                if (finalEntryLower in listOf("balintawak", "mindanao avenue") && currentBoothLower in nlexOpenExits) {
                    if (currentBoothLower in nlexFlat85Exits) finalExit = "Marilao"
                }

                if (finalEntryLower in nlexOpenEntries && currentBoothLower.contains("balintawak")) {
                    if (finalEntryLower in nlexFlat85Exits) finalEntry = "Marilao"
                }

                val checkEntryLower = finalEntry.lowercase()
                val checkExitLower = finalExit.lowercase()

                val isCavitexEntry = cavitexMainline.any { checkEntryLower.contains(it) } || cavitexSouthLink.any { checkEntryLower.contains(it) }
                val isCavitexExit = cavitexMainline.any { checkExitLower.contains(it) } || cavitexSouthLink.any { checkExitLower.contains(it) }

                if (isCavitexEntry && isCavitexExit) {
                    val resolved = resolveCavitexLeg(finalEntry, finalExit)
                    finalEntry = resolved.entry
                    finalExit = resolved.exit
                }

                legs.add(BilledLeg(entry = finalEntry, exit = finalExit, note = "Final Route Leg"))
                break
            }

            i++
        }

        if (officialBooths.size == 1) {
            val singleBooth = officialBooths.first()
            val lower = singleBooth.lowercase()

            if (cavitexMainline.any { lower.contains(it) } || cavitexSouthLink.any { lower.contains(it) }) {
                legs.add(BilledLeg(entry = singleBooth, exit = getCavitexStandaloneExit(singleBooth), note = "Rule D3: CAVITEX Single Pass"))
            } else {
                legs.add(BilledLeg(entry = singleBooth, exit = singleBooth, note = "Rule D1: Flat Rate / Single Pass"))
            }
        }

        return legs
    }
}