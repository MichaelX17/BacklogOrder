package com.backlogorder.domain.import

import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.ListGameEntity
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.ExportedGame
import com.backlogorder.domain.ExportedList
import com.backlogorder.domain.ExportedListGame
import com.backlogorder.domain.GameContentHasher
import com.backlogorder.domain.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MergePlanBuilderTest {

    private val listId1 = UUID.randomUUID()
    private val gameId1 = UUID.randomUUID()
    private val linkId1 = UUID.randomUUID()

    @Test
    fun buildMergePlan_identicalLibrary_returnsNoAddsNoConflictsAllSkipped(): Unit {
        val now = 1000L
        val listEntity = ListEntity(id = listId1.toString(), name = "Backlog", createdAt = now, updatedAt = now)
        val gameEntity = GameEntity(
            id = gameId1.toString(),
            rawgId = 42,
            name = "Portal 2",
            backgroundImage = null,
            backgroundImageUrl = "https://example.com/portal.jpg",
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = "[\"Puzzle\"]",
            platforms = "[\"PC\"]",
            tags = "[\"co-op\"]",
            isManual = 0,
            franchiseName = "Portal",
            franchiseOrder = 2,
            createdAt = now,
            updatedAt = now,
        )
        val listGameEntity = ListGameEntity(
            id = linkId1.toString(),
            listId = listId1.toString(),
            gameId = gameId1.toString(),
            status = GameStatus.BACKLOG.name,
            addedAt = now,
            listName = "Backlog",
        )

        val local = LocalSnapshot(
            lists = mapOf(listEntity.id to listEntity),
            games = mapOf(gameEntity.id to gameEntity),
            gamesByRawgId = mapOf(42 to gameEntity),
            listGames = mapOf(listGameEntity.id to listGameEntity),
            listGamesByPair = mapOf(Pair(listId1.toString(), gameId1.toString()) to listGameEntity),
        )

        val exportedGame = ExportedGame(
            id = gameId1,
            rawgId = 42,
            gameName = "Portal 2",
            backgroundImage = null,
            backgroundImageUrl = "https://example.com/portal.jpg",
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = listOf("Puzzle"),
            platforms = listOf("PC"),
            tags = listOf("co-op"),
            isManual = false,
            franchiseName = "Portal",
            franchiseOrder = 2,
            createdAt = now,
            updatedAt = now,
            contentHash = GameContentHasher.computeHash("Portal 2", 42, 95, 4.8, 6.0, listOf("Puzzle"), listOf("PC"), listOf("co-op"), false, "Portal", 2),
        )

        val exportedList = ExportedList(id = listId1, listName = "Backlog", createdAt = now, updatedAt = now)
        val exportedLink = ExportedListGame(id = linkId1, listId = listId1, gameId = gameId1, status = GameStatus.BACKLOG, addedAt = now)

        val backup = ExportData(
            lists = listOf(exportedList),
            games = listOf(exportedGame),
            listGames = listOf(exportedLink),
        )

        val plan = MergePlanBuilder.buildMergePlan(backup, local)

        assertTrue(plan.toAddLists.isEmpty())
        assertTrue(plan.toAddGames.isEmpty())
        assertTrue(plan.toAddListGames.isEmpty())
        assertTrue(plan.conflicts.isEmpty())
        assertEquals(1, plan.skippedLists)
        assertEquals(1, plan.skippedGames)
        assertEquals(1, plan.skippedMemberships)
        assertTrue(plan.isNoOp)
    }

    @Test
    fun buildMergePlan_gameContentDiffers_createsMetadataConflict(): Unit {
        val now = 1000L
        val listEntity = ListEntity(id = listId1.toString(), name = "Backlog", createdAt = now, updatedAt = now)
        val gameEntity = GameEntity(
            id = gameId1.toString(),
            rawgId = 42,
            name = "Portal 2",
            backgroundImage = null,
            backgroundImageUrl = null,
            metacritic = 80, // Different metacritic score
            rating = 4.0,
            playtimeHours = 5.0,
            genres = "[]",
            platforms = "[]",
            tags = "[]",
            isManual = 0,
            franchiseName = null,
            franchiseOrder = null,
            createdAt = now,
            updatedAt = now,
        )

        val local = LocalSnapshot(
            lists = mapOf(listEntity.id to listEntity),
            games = mapOf(gameEntity.id to gameEntity),
            gamesByRawgId = mapOf(42 to gameEntity),
        )

        val exportedGame = ExportedGame(
            id = gameId1,
            rawgId = 42,
            gameName = "Portal 2",
            metacritic = 95,
            rating = 4.8,
            playtimeHours = 6.0,
            genres = emptyList(),
            platforms = emptyList(),
            tags = emptyList(),
            isManual = false,
            createdAt = now,
            updatedAt = now,
            contentHash = "different_hash",
        )

        val backup = ExportData(
            lists = listOf(ExportedList(id = listId1, listName = "Backlog", createdAt = now, updatedAt = now)),
            games = listOf(exportedGame),
        )

        val plan = MergePlanBuilder.buildMergePlan(backup, local)

        assertEquals(1, plan.conflicts.size)
        assertTrue(plan.conflicts.first() is ImportConflict.GameMetadata)
    }

    @Test
    fun buildMergePlan_sameRawgIdDifferentUuid_createsRawgIdCollisionAndRepointsGameId(): Unit {
        val now = 1000L
        val localGameId = UUID.randomUUID()
        val backupGameId = UUID.randomUUID()

        val gameEntity = GameEntity(
            id = localGameId.toString(),
            rawgId = 100,
            name = "Hades",
            backgroundImage = null,
            metacritic = 93,
            rating = 4.9,
            playtimeHours = 40.0,
            franchiseName = null,
            franchiseOrder = null,
            createdAt = now,
            updatedAt = now,
        )

        val local = LocalSnapshot(
            lists = mapOf(listId1.toString() to ListEntity(listId1.toString(), "Backlog", now, now)),
            games = mapOf(gameEntity.id to gameEntity),
            gamesByRawgId = mapOf(100 to gameEntity),
        )

        val exportedGame = ExportedGame(
            id = backupGameId,
            rawgId = 100,
            gameName = "Hades (Backup)",
            metacritic = 90,
            rating = 4.5,
            playtimeHours = 20.0,
            genres = emptyList(),
            platforms = emptyList(),
            tags = emptyList(),
            isManual = false,
            createdAt = now,
            updatedAt = now,
            contentHash = "backup_hash",
        )

        val exportedLink = ExportedListGame(
            id = UUID.randomUUID(),
            listId = listId1,
            gameId = backupGameId,
            status = GameStatus.PLAYING,
            addedAt = now,
        )

        val backup = ExportData(
            lists = listOf(ExportedList(id = listId1, listName = "Backlog", createdAt = now, updatedAt = now)),
            games = listOf(exportedGame),
            listGames = listOf(exportedLink),
        )

        val plan = MergePlanBuilder.buildMergePlan(backup, local)

        assertEquals(localGameId.toString(), plan.backupToEffectiveGameId[backupGameId.toString()])
        assertEquals(1, plan.conflicts.size)
        assertTrue(plan.conflicts.first() is ImportConflict.GameRawgId)
    }
}
