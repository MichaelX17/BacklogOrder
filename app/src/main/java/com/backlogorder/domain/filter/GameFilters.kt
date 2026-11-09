package com.backlogorder.domain.filter

import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.score.ScoreCalculator

object GameFilters {
    fun matches(entry: ListEntry, filter: FilterState): Boolean {
        val game = entry.game

        if (filter.name.isNotBlank() && !game.name.contains(filter.name, ignoreCase = true)) {
            return false
        }

        if (!matchesRange(game.playtimeHours, filter.playtimeMin, filter.playtimeMax)) {
            return false
        }

        val effectiveRating = game.metacritic?.toDouble() ?: game.rating?.let { it * 20.0 }
        if (!matchesRange(effectiveRating, filter.ratingMin, filter.ratingMax)) {
            return false
        }

        val score = ScoreCalculator.computeScore(game)
        if (!matchesRange(score, filter.scoreMin, filter.scoreMax)) {
            return false
        }

        if (filter.statuses.isNotEmpty() && entry.status !in filter.statuses) {
            return false
        }

        return true
    }

    fun filter(entries: List<ListEntry>, filter: FilterState): List<ListEntry> {
        return entries.filter { matches(it, filter) }
    }

    private fun matchesRange(value: Double?, min: Double?, max: Double?): Boolean {
        if (value == null) {
            return min == null && max == null
        }

        val minCheck = min == null || value >= min
        val maxCheck = max == null || value <= max
        return minCheck && maxCheck
    }
}
