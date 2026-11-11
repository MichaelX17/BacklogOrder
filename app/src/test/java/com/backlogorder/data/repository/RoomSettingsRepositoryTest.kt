package com.backlogorder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.domain.error.AppError.Validation
import com.backlogorder.domain.repository.ApiKeyProvider
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomSettingsRepositoryTest {
    private lateinit var database: BacklogOrderDatabase
    private lateinit var repository: RoomSettingsRepository
    private lateinit var context: Context
    private lateinit var apiKeyProvider: ApiKeyProvider
    private lateinit var scope: TestScope

    @Before
    fun setup() {
        scope = TestScope()
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, BacklogOrderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        apiKeyProvider = object : ApiKeyProvider {
            override val key = flowOf(null as String?)
            override suspend fun setKey(value: String) {}
            override suspend fun clear() {}
        }
        repository = RoomSettingsRepository(context, database, apiKeyProvider)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `validateExportSchema passes for valid data`() = scope.runTest {
        val result = repository.validateExportSchema()
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `exportLocalPath nulls non existent or empty files`() = scope.runTest {
        assertNull(repository.exportLocalPath("/non/existent/file.jpg"))

        val emptyFile = File.createTempFile("empty", ".jpg")
        assertNull(repository.exportLocalPath(emptyFile.absolutePath))
        emptyFile.delete()

        val validFile = File.createTempFile("valid", ".jpg")
        validFile.writeText("test content")
        assertEquals(validFile.absolutePath, repository.exportLocalPath(validFile.absolutePath))
        validFile.delete()
    }

    @Test
    fun `exportData includes both RAWG and manual games`() = scope.runTest {
        val now = System.currentTimeMillis()
        database.listDao().insertList(ListEntity("list-1", "Test List", now, now))
        database.gamesDao().insertGame(
            GameEntity(
                id = "game-rawg",
                rawgId = 100,
                name = "RAWG Game",
                backgroundImage = null,
                backgroundImageUrl = "https://example.com/rawg.jpg",
                metacritic = 85,
                rating = 4.2,
                playtimeHours = 12.0,
                isManual = 0,
                franchiseName = null,
                franchiseOrder = null,
                createdAt = now,
                updatedAt = now,
            )
        )
        database.gamesDao().insertGame(
            GameEntity(
                id = "game-manual",
                rawgId = null,
                name = "Manual Game",
                backgroundImage = null,
                backgroundImageUrl = null,
                metacritic = null,
                rating = null,
                playtimeHours = null,
                isManual = 1,
                franchiseName = null,
                franchiseOrder = null,
                createdAt = now,
                updatedAt = now,
            )
        )

        database.gamesDao().insertListGame(
            com.backlogorder.data.local.ListGameEntity(
                id = "link-1",
                listId = "list-1",
                gameId = "game-rawg",
                status = "BACKLOG",
                addedAt = now,
                listName = "Test List",
            )
        )
        database.gamesDao().insertListGame(
            com.backlogorder.data.local.ListGameEntity(
                id = "link-2",
                listId = "list-1",
                gameId = "game-manual",
                status = "PLAYING",
                addedAt = now,
                listName = "Test List",
            )
        )

        val result = repository.exportData()
        assertTrue(result.isSuccess)
        val exportData = result.getOrThrow()
        assertEquals(2, exportData.games.size)
    }

    @Test
    fun `importData returns failure when validation fails`() = scope.runTest {
        val invalidJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [],
            "games": [],
            "listGames": []
        }
        """.trimIndent()
        
        val result = repository.importData(invalidJson)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Validation)
        assertTrue(result.exceptionOrNull()?.message?.contains("at least one list") == true)
    }

    @Test
    fun `importData rejects invalid enum value with coerceInputValues false`() = scope.runTest {
        val invalidEnumJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [{"id": "00000000-0000-0000-0000-000000000001", "name": "List", "createdAt": 1000, "updatedAt": 1000}],
            "games": [{"id": "00000000-0000-0000-0000-000000000002", "name": "Game", "genres": [], "platforms": [], "tags": [], "isManual": true, "createdAt": 1000, "updatedAt": 1000, "contentHash": "hash"}],
            "listGames": [{"id": "00000000-0000-0000-0000-000000000003", "listId": "00000000-0000-0000-0000-000000000001", "gameId": "00000000-0000-0000-0000-000000000002", "status": "INVALID_STATUS", "addedAt": 1000}]
        }
        """.trimIndent()

        val result = repository.importData(invalidEnumJson)
        assertTrue(result.isFailure)
    }

    @Test
    fun `import valid backup into empty database in Merge mode succeeds`() = scope.runTest {
        val validJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [{"id": "00000000-0000-0000-0000-000000000001", "name": "Backlog", "createdAt": 1000, "updatedAt": 1000}],
            "games": [{"id": "00000000-0000-0000-0000-000000000002", "name": "Celeste", "genres": [], "platforms": [], "tags": [], "isManual": true, "createdAt": 1000, "updatedAt": 1000, "contentHash": "hash"}],
            "listGames": [{"id": "00000000-0000-0000-0000-000000000003", "listId": "00000000-0000-0000-0000-000000000001", "gameId": "00000000-0000-0000-0000-000000000002", "status": "BACKLOG", "addedAt": 1000}]
        }
        """.trimIndent()

        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = false }
        val backupData = json.decodeFromString<com.backlogorder.domain.ExportData>(validJson)
        val localSnapshot = repository.createLocalSnapshot()

        val plan = com.backlogorder.domain.import.MergePlanBuilder.buildMergePlan(backupData, localSnapshot)
        val applyResult = repository.applyMergePlan(plan, emptyMap())

        assertTrue(applyResult.isSuccess)
        val report = applyResult.getOrThrow()
        assertEquals(1, report.addedLists)
        assertEquals(1, report.addedGames)
        assertEquals(1, report.addedMemberships)
    }

    @Test
    fun `import valid backup into empty database in Restore mode succeeds`() = scope.runTest {
        val validJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [{"id": "00000000-0000-0000-0000-000000000001", "name": "Backlog", "createdAt": 1000, "updatedAt": 1000}],
            "games": [{"id": "00000000-0000-0000-0000-000000000002", "name": "Celeste", "genres": [], "platforms": [], "tags": [], "isManual": true, "createdAt": 1000, "updatedAt": 1000, "contentHash": "hash"}],
            "listGames": [{"id": "00000000-0000-0000-0000-000000000003", "listId": "00000000-0000-0000-0000-000000000001", "gameId": "00000000-0000-0000-0000-000000000002", "status": "BACKLOG", "addedAt": 1000}]
        }
        """.trimIndent()

        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = false }
        val backupData = json.decodeFromString<com.backlogorder.domain.ExportData>(validJson)

        val result = repository.applyRestorePlan(backupData)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `import valid backup into populated database still works`() = scope.runTest {
        val now = System.currentTimeMillis()
        database.listDao().insertList(ListEntity("00000000-0000-0000-0000-000000000099", "Existing List", now, now))

        val validJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [{"id": "00000000-0000-0000-0000-000000000001", "name": "Backlog", "createdAt": 1000, "updatedAt": 1000}],
            "games": [{"id": "00000000-0000-0000-0000-000000000002", "name": "Celeste", "genres": [], "platforms": [], "tags": [], "isManual": true, "createdAt": 1000, "updatedAt": 1000, "contentHash": "hash"}],
            "listGames": [{"id": "00000000-0000-0000-0000-000000000003", "listId": "00000000-0000-0000-0000-000000000001", "gameId": "00000000-0000-0000-0000-000000000002", "status": "BACKLOG", "addedAt": 1000}]
        }
        """.trimIndent()

        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = false }
        val backupData = json.decodeFromString<com.backlogorder.domain.ExportData>(validJson)
        val localSnapshot = repository.createLocalSnapshot()

        val plan = com.backlogorder.domain.import.MergePlanBuilder.buildMergePlan(backupData, localSnapshot)
        val result = repository.applyMergePlan(plan, emptyMap())

        assertTrue(result.isSuccess)
    }

    @Test
    fun `import backup with zero lists is rejected by validation`() = scope.runTest {
        val zeroListsJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [],
            "games": [],
            "listGames": []
        }
        """.trimIndent()

        val result = repository.importData(zeroListsJson)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Validation)
    }
}
