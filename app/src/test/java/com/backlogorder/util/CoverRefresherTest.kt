package com.backlogorder.util

import androidx.test.core.app.ApplicationProvider
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.repository.RoomGamesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoverRefresherTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun requestRefresh_downloadsCoverAndUpdatesRepository(): Unit = testScope.runTest {
        var downloadCalls = 0
        var updatedLocalPath: String? = null

        val fakeDownloader = object : CoverImageDownloaderFake() {
            override suspend fun download(gameId: String, imageUrl: String?): Result<String> {
                downloadCalls++
                return Result.success("/files/covers/cover_game-123.jpg")
            }
        }

        val gameEntity = GameEntity(
            id = "game-123",
            rawgId = 123,
            name = "Test Game",
            backgroundImage = null,
            backgroundImageUrl = "https://example.com/cover.jpg",
            metacritic = null,
            rating = null,
            playtimeHours = null,
            franchiseName = null,
            franchiseOrder = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val fakeRepo = object : RoomGamesRepositoryFake(gameEntity) {
            override suspend fun updateCoverFields(gameId: String, localPath: String?, url: String?) {
                updatedLocalPath = localPath
            }
        }

        val refresher = CoverRefresher(
            downloader = fakeDownloader,
            gamesRepository = fakeRepo,
            scope = testScope,
            cooldownMs = 5000L,
        )

        refresher.requestRefresh("game-123")

        assertEquals(1, downloadCalls)
        assertEquals("/files/covers/cover_game-123.jpg", updatedLocalPath)
    }

    @Test
    fun requestRefresh_throttlesCallsWithinCooldown(): Unit = testScope.runTest {
        var downloadCalls = 0

        val fakeDownloader = object : CoverImageDownloaderFake() {
            override suspend fun download(gameId: String, imageUrl: String?): Result<String> {
                downloadCalls++
                return Result.success("/files/covers/cover_game-456.jpg")
            }
        }

        val gameEntity = GameEntity(
            id = "game-456",
            rawgId = 456,
            name = "Test Game 2",
            backgroundImage = null,
            backgroundImageUrl = "https://example.com/cover2.jpg",
            metacritic = null,
            rating = null,
            playtimeHours = null,
            franchiseName = null,
            franchiseOrder = null,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val fakeRepo = object : RoomGamesRepositoryFake(gameEntity) {}

        val refresher = CoverRefresher(
            downloader = fakeDownloader,
            gamesRepository = fakeRepo,
            scope = testScope,
            cooldownMs = 60_000L,
        )

        refresher.requestRefresh("game-456")
        refresher.requestRefresh("game-456")

        assertEquals(1, downloadCalls)
    }
}

open class CoverImageDownloaderFake : CoverImageDownloader(
    context = ApplicationProvider.getApplicationContext(),
)

open class RoomGamesRepositoryFake(private val entity: GameEntity) : RoomGamesRepository(
    database = androidx.room.Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        BacklogOrderDatabase::class.java,
    ).build(),
) {
    override suspend fun getGameById(gameId: String): GameEntity? = if (entity.id == gameId) entity else null
    override suspend fun updateCoverFields(gameId: String, localPath: String?, url: String?) {}
}
