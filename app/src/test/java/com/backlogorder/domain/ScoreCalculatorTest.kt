package com.backlogorder.domain

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.score.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScoreCalculatorTest {
    @Test
    fun `computeScoreReturnsMetacriticDividedByPlaytime`() {
        val game = Game(
            id = "1",
            name = "Score test",
            metacritic = 80,
            rating = 4.5,
            playtimeHours = 4.0,
        )

        assertEquals(20.0, ScoreCalculator.computeScore(game) ?: 0.0, 0.0001)
    }

    @Test
    fun `computeScoreReturnsNullWhenMetacriticIsMissing`() {
        val game = Game(
            id = "2",
            name = "No metacritic",
            rating = 4.8,
            playtimeHours = 5.0,
        )

        assertNull(ScoreCalculator.computeScore(game))
    }

    @Test
    fun `computeScoreReturnsNullForZeroOrNegativePlaytime`() {
        val gameZero = Game(id = "3", name = "Zero duration", metacritic = 90, playtimeHours = 0.0)
        assertNull(ScoreCalculator.computeScore(gameZero))

        val gameNegative = Game(id = "4", name = "Negative duration", metacritic = 90, playtimeHours = -2.0)
        assertNull(ScoreCalculator.computeScore(gameNegative))
    }

    @Test
    fun `computeScoreReturnsNullForInvalidMetacritic`() {
        val gameInvalid = Game(id = "5", name = "Invalid metacritic", metacritic = 105, playtimeHours = 10.0)
        assertNull(ScoreCalculator.computeScore(gameInvalid))
    }

    @Test
    fun `scoreDisplayReturnsDashForMissingScore`() {
        assertEquals("—", ScoreCalculator.scoreDisplay(null))
    }

    @Test
    fun `scoreDisplayFormatsScoreToTwoDecimalPlaces`() {
        assertEquals("2.40", ScoreCalculator.scoreDisplay(2.4))
        assertEquals("11.25", ScoreCalculator.scoreDisplay(11.25))
    }
}
