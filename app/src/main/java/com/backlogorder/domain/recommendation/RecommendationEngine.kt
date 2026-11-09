package com.backlogorder.domain.recommendation

import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.score.ScoreCalculator

class RecommendationEngine(
    private val scoreCalculator: ScoreCalculator = ScoreCalculator,
) {
    fun findRecommendation(entries: List<ListEntry>, skipCount: Int): ListEntry? {
        val sorted = sortedCandidates(entries)
        if (sorted.isEmpty()) return null
        val effectiveIndex = (skipCount % sorted.size + sorted.size) % sorted.size
        return sorted[effectiveIndex]
    }

    fun upcoming(entries: List<ListEntry>, skipCount: Int, count: Int = 2): List<ListEntry> {
        val sorted = sortedCandidates(entries)
        if (sorted.size <= 1) return emptyList()
        val effectiveIndex = (skipCount % sorted.size + sorted.size) % sorted.size
        val result = mutableListOf<ListEntry>()
        for (i in 1..count.coerceAtMost(sorted.size - 1)) {
            val idx = (effectiveIndex + i) % sorted.size
            result.add(sorted[idx])
        }
        return result
    }

    private fun sortedCandidates(entries: List<ListEntry>): List<ListEntry> {
        return entries
            .asSequence()
            .filter { it.status == GameStatus.PLAYING || it.status == GameStatus.BACKLOG }
            .filter { scoreCalculator.computeScore(it.game) != null }
            .sortedWith(
                compareBy<ListEntry> { if (it.status == GameStatus.PLAYING) 0 else 1 }
                    .thenByDescending { scoreCalculator.computeScore(it.game)!! }
                    .thenBy { it.game.name.lowercase() }
                    .thenBy { it.id },
            )
            .toList()
    }
}
