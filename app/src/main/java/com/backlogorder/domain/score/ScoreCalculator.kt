package com.backlogorder.domain.score

import com.backlogorder.domain.model.Game
import java.util.Locale

object ScoreCalculator {
    fun computeScore(game: Game): Double? {
        val metacritic = game.metacritic ?: return null
        if (metacritic !in 0..100) {
            return null
        }

        val playtimeHours = game.playtimeHours ?: return null
        if (playtimeHours <= 0.0) {
            return null
        }

        return metacritic.toDouble() / playtimeHours
    }

    fun scoreDisplay(score: Double?, playtimeHours: Double? = null, isInList: Boolean = false): String {
        if (score == null) {
            return "—"
        }
        return String.format(Locale.US, "%.2f", score)
    }
}
