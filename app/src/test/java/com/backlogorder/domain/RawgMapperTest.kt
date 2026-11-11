package com.backlogorder.domain

import com.backlogorder.domain.mapper.RawgMapper
import com.backlogorder.domain.model.RawgGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RawgMapperTest {
    @Test
    fun `mapRawgGameCopiesCoreFieldsAndDisablesManualFlag`() {
        val rawgGame = RawgGame(
            rawgId = 42,
            name = "Portal 2",
            backgroundImage = "https://image.url",
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = listOf("Puzzle"),
            platforms = listOf("PC"),
            tags = listOf("co-op"),
        )

        val mapped = RawgMapper.mapRawgGame(rawgGame)

        assertEquals(42, mapped.rawgId)
        assertEquals("Portal 2", mapped.name)
        assertEquals(null, mapped.backgroundImage)
        assertEquals("https://image.url", mapped.backgroundImageUrl)
        assertEquals(95, mapped.metacritic)
        assertEquals(4.8, mapped.rating ?: 0.0, 0.0001)
        assertEquals(6.0, mapped.playtimeHours ?: 0.0, 0.0001)
        assertEquals(listOf("Puzzle"), mapped.genres)
        assertFalse(mapped.isManual)
    }
}
