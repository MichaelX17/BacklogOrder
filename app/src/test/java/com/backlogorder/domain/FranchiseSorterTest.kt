package com.backlogorder.domain

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.sort.FranchiseSorter
import org.junit.Assert.assertEquals
import org.junit.Test

class FranchiseSorterTest {
    @Test
    fun `groupsEntriesByFranchiseAndOrdersByQualityIndex`() {
        val entries = listOf(
            ListEntry("1", "list", Game("1", name = "Alpha", franchiseName = "Legend", franchiseOrder = 2, metacritic = 90, playtimeHours = 5.0), GameStatus.BACKLOG),
            ListEntry("2", "list", Game("2", name = "Beta", franchiseName = "Legend", franchiseOrder = 1, metacritic = 70, playtimeHours = 8.0), GameStatus.PLAYING),
            ListEntry("3", "list", Game("3", name = "Gamma", franchiseName = "Pixel", metacritic = 95, playtimeHours = 2.0), GameStatus.BACKLOG),
            ListEntry("4", "list", Game("4", name = "Delta", metacritic = 60, playtimeHours = 4.0), GameStatus.PLAYING),
        )

        val groups = FranchiseSorter.sort(entries)

        assertEquals(listOf("Pixel", "Legend", "Delta"), groups.map { it.displayName })
        assertEquals(listOf("Gamma", "Beta", "Alpha", "Delta"), groups.flatMap { it.entries.map { entry -> entry.game.name } })
    }

    @Test
    fun `duplicateDisplayNames_produceUniqueGroupIds`() {
        val entries = listOf(
            ListEntry("e1", "list1", Game("g1", name = "Zelda"), GameStatus.BACKLOG),
            ListEntry("e2", "list2", Game("g2", name = "Zelda"), GameStatus.BACKLOG),
        )

        val groups = FranchiseSorter.sort(entries)

        assertEquals(2, groups.size)
        assertEquals("Zelda", groups[0].displayName)
        assertEquals("Zelda", groups[1].displayName)
        val ids = groups.map { it.id }
        assertEquals(2, ids.toSet().size)
    }
}
