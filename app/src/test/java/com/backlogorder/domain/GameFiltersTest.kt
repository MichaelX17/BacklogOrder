package com.backlogorder.domain

import com.backlogorder.domain.filter.FilterState
import com.backlogorder.domain.filter.GameFilters
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFiltersTest {
    @Test
    fun `matchesNameAndStatusAndRangeFilters`() {
        val entry = ListEntry(
            id = "games-1",
            listId = "list-1",
            game = Game(
                id = "game-1",
                name = "Zelda",
                metacritic = 85,
                playtimeHours = 8.0,
            ),
            status = GameStatus.PLAYING,
        )

        val filter = FilterState(
            name = "zel",
            playtimeMin = 4.0,
            ratingMin = 80.0,
            scoreMin = 10.0,
            statuses = setOf(GameStatus.PLAYING),
        )

        assertTrue(GameFilters.matches(entry, filter))
    }

    @Test
    fun `matchesExcludesEntriesWhenBoundedNumericValueIsMissing`() {
        val entry = ListEntry(
            id = "games-2",
            listId = "list-1",
            game = Game(id = "game-2", name = "No Rating", playtimeHours = 3.0),
            status = GameStatus.BACKLOG,
        )

        val filter = FilterState(ratingMin = 70.0)
        assertFalse(GameFilters.matches(entry, filter))
    }
}
