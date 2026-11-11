package com.backlogorder.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GameContentHasherTest {

    @Test
    fun computeHash_producesIdenticalHashForSameContent() {
        val hash1 = GameContentHasher.computeHash(
            name = "Portal 2",
            rawgId = 42,
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = listOf("Action", "Puzzle"),
            platforms = listOf("PC", "PlayStation"),
            tags = listOf("co-op", "first-person"),
            isManual = false,
            franchiseName = "Portal",
            franchiseOrder = 2,
        )

        val hash2 = GameContentHasher.computeHash(
            name = "Portal 2",
            rawgId = 42,
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = listOf("Puzzle", "Action"),
            platforms = listOf("PlayStation", "PC"),
            tags = listOf("first-person", "co-op"),
            isManual = false,
            franchiseName = "Portal",
            franchiseOrder = 2,
        )

        assertEquals(hash1, hash2)
    }

    @Test
    fun computeHash_changesHashWhenContentChanges() {
        val hash1 = GameContentHasher.computeHash(
            name = "Portal 2",
            rawgId = 42,
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = listOf("Puzzle"),
            platforms = listOf("PC"),
            tags = listOf("co-op"),
            isManual = false,
            franchiseName = "Portal",
            franchiseOrder = 2,
        )

        val hash2 = GameContentHasher.computeHash(
            name = "Portal 2",
            rawgId = 42,
            metacritic = 95,
            rating = 5.0,
            playtimeHours = 6.0,
            genres = listOf("Puzzle"),
            platforms = listOf("PC"),
            tags = listOf("co-op"),
            isManual = false,
            franchiseName = "Portal",
            franchiseOrder = 2,
        )

        assertNotEquals(hash1, hash2)
    }
}
