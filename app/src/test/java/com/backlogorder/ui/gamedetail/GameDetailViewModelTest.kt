package com.backlogorder.ui.gamedetail

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.GamesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun reloadRawgCoverCancellationDoesNotCrash() = runTest {
        val entry = ListEntry(
            id = "entry-1",
            listId = "list-1",
            game = Game(id = "game-1", rawgId = 123, name = "Test Game"),
            status = GameStatus.BACKLOG,
        )
        val gamesRepo = object : GamesRepository {
            override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = MutableStateFlow(listOf(entry))
            override fun observeAllEntries(): Flow<List<ListEntry>> = emptyFlow()
            override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus) = entry
            override suspend fun removeGameFromList(listId: String, gameId: String) = Unit
            override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) = Unit
            override suspend fun updateGame(game: Game) = Unit
            override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
        }

        val apiKeyProvider = object : ApiKeyProvider {
            override val key: Flow<String?> = MutableStateFlow("test-key")
            override val currentKey: String? get() = "test-key"
            override suspend fun setKey(value: String) = Unit
            override suspend fun clear() = Unit
        }

        val viewModel = GameDetailViewModel(
            listId = "list-1",
            gameId = "game-1",
            gamesRepository = gamesRepo,
            rawgRemoteDataSource = null,
            apiKeyProvider = apiKeyProvider,
        )

        advanceUntilIdle()

        val job = backgroundScope.launch {
            viewModel.reloadRawgCover()
        }
        job.cancel()
        advanceUntilIdle()

        assertTrue(job.isCancelled)
    }
}
