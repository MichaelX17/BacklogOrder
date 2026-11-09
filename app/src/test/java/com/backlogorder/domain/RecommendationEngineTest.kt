package com.backlogorder.domain

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.recommendation.RecommendationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecommendationEngineTest {
    private val engine = RecommendationEngine()

    @Test
    fun `findRecommendationPrefersPlayingStatusAndHigherScores`() {
        val entries = listOf(
            listEntry("1", "list", "Alpha", 90, 2.0, GameStatus.BACKLOG),
            listEntry("2", "list", "Beta", 100, 5.0, GameStatus.PLAYING),
            listEntry("3", "list", "Gamma", 70, 1.0, GameStatus.PLAYING),
        )

        val recommendation = engine.findRecommendation(entries, 0)

        assertNotNull(recommendation)
        assertEquals("Gamma", recommendation?.game?.name)
    }

    @Test
    fun `skippingWrapsAroundInfinitely`() {
        val entries = listOf(
            listEntry("1", "list", "Alpha", 90, 2.0, GameStatus.PLAYING),
            listEntry("2", "list", "Beta", 80, 2.0, GameStatus.PLAYING),
        )

        assertEquals("Alpha", engine.findRecommendation(entries, 0)?.game?.name)
        assertEquals("Beta", engine.findRecommendation(entries, 1)?.game?.name)
        // Wraps around
        assertEquals("Alpha", engine.findRecommendation(entries, 2)?.game?.name)
        assertEquals("Beta", engine.findRecommendation(entries, 3)?.game?.name)
    }

    @Test
    fun `upcomingReturnsNextCandidatesFromSameSortedOrder`() {
        val entries = listOf(
            listEntry("4", "list", "Druid", 80, 2.0, GameStatus.BACKLOG),
            listEntry("5", "list", "Elder", 90, 2.0, GameStatus.PLAYING),
            listEntry("6", "list", "Forest", 70, 1.0, GameStatus.PLAYING),
            listEntry("7", "list", "Glade", 60, 3.0, GameStatus.BACKLOG),
        )

        val upcoming = engine.upcoming(entries, 0, 2)

        assertEquals(listOf("Elder", "Druid"), upcoming.map { it.game.name })
    }

    private fun listEntry(
        id: String,
        listId: String,
        name: String,
        metacritic: Int,
        playtime: Double,
        status: GameStatus,
    ): ListEntry {
        val game = Game(
            id = id,
            name = name,
            metacritic = metacritic,
            playtimeHours = playtime,
        )
        return ListEntry(id = id, listId = listId, game = game, status = status)
    }
}
