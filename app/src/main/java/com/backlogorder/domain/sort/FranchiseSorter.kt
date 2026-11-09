package com.backlogorder.domain.sort

import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.score.ScoreCalculator

object FranchiseSorter {
    private val scoreCalculator: ScoreCalculator = ScoreCalculator

    fun franchiseKey(entry: ListEntry): String {
        return entry.game.franchiseName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "standalone:${entry.game.id}"
    }

    fun sort(entries: List<ListEntry>): List<FranchiseGroup> {
        val grouped = entries.groupBy { franchiseKey(it) }
        return grouped
            .map { (key, groupEntries) ->
                val isStandalone = key.startsWith("standalone:")
                val displayName = if (isStandalone) {
                    groupEntries.firstOrNull()?.game?.name ?: "Standalone"
                } else {
                    key
                }
                val id = if (isStandalone) {
                    val gameId = groupEntries.firstOrNull()?.game?.id ?: key
                    "game:$gameId"
                } else {
                    "franchise:$key"
                }
                FranchiseGroup(
                    id = id,
                    key = key,
                    displayName = displayName,
                    entries = groupEntries.sortedWith(groupComparator()),
                )
            }
            .sortedWith(
                compareByDescending<FranchiseGroup> { groupMaxScore(it.entries) }
                    .thenBy { it.displayName.lowercase() },
            )
    }

    private fun groupComparator(): Comparator<ListEntry> {
        return compareBy<ListEntry> { entry ->
            if (entry.game.franchiseOrder != null) 0 else 1
        }
            .thenBy { entry -> entry.game.franchiseOrder ?: Int.MAX_VALUE }
            .thenByDescending { scoreCalculator.computeScore(it.game) ?: Double.NEGATIVE_INFINITY }
            .thenBy { it.game.name.lowercase() }
            .thenBy { it.id }
    }

    private fun groupMaxScore(entries: List<ListEntry>): Double {
        return entries.maxOfOrNull { entry -> scoreCalculator.computeScore(entry.game) ?: Double.NEGATIVE_INFINITY }
            ?: Double.NEGATIVE_INFINITY
    }
}
