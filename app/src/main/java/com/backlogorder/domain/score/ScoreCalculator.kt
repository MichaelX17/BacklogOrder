package com.backlogorder.domain.score

import com.backlogorder.domain.model.Game
import java.util.Locale

object ScoreCalculator {
    fun effectiveMetacritic(game: Game): Double? = effectiveMetacritic(game.metacritic, game.rating)

    fun effectiveMetacritic(metacritic: Int?, rating: Double?): Double? {
        if (metacritic != null && metacritic in 0..100) {
            return metacritic.toDouble()
        }
        if (rating != null && rating in 0.0..5.0) {
            return rating * 20.0
        }
        return null
    }

    fun computeScore(game: Game): Double? {
        val scoreValue = effectiveMetacritic(game) ?: return null

        val playtimeHours = game.playtimeHours ?: return null
        if (playtimeHours <= 0.0) {
            return null
        }

        return scoreValue / playtimeHours
    }

    fun scoreDisplay(score: Double?, playtimeHours: Double? = null, isInList: Boolean = false): String {
        if (score == null) {
            return "—"
        }
        return String.format(Locale.US, "%.2f", score)
    }
}
