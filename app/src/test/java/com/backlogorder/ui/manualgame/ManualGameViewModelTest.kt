package com.backlogorder.ui.manualgame

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.repository.GamesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManualGameViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun invalidNameIsRejected() = runTest {
        val repo = FakeManualGamesRepository()
        val viewModel = ManualGameViewModel(listId = "list-1", gamesRepository = repo)

        viewModel.onNameChanged("   ")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.nameError != null)
        assertEquals(0, repo.entries.size)
    }

    @Test
    fun outOfRangeMetacriticIsRejected() = runTest {
        val repo = FakeManualGamesRepository()
        val viewModel = ManualGameViewModel(listId = "list-1", gamesRepository = repo)

        viewModel.onNameChanged("Portal")
        viewModel.onMetacriticChanged("999")
        viewModel.submit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.metacriticError != null)
        assertEquals(0, repo.entries.size)
    }

    @Test
    fun validGameIsAddedToListAsBacklog() = runTest {
        val repo = FakeManualGamesRepository()
        val viewModel = ManualGameViewModel(listId = "list-1", gamesRepository = repo)

        viewModel.onNameChanged("Portal 2")
        viewModel.onMetacriticChanged("89")
        viewModel.onRatingChanged("4.5")
        viewModel.onPlaytimeHoursChanged("7.5")
        viewModel.onFranchiseNameChanged("Portal")
        viewModel.onFranchiseOrderChanged("2")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, repo.entries.size)
        assertEquals("Portal 2", repo.entries.first().game.name)
        assertEquals(GameStatus.BACKLOG, repo.entries.first().status)
        assertTrue(repo.entries.first().game.isManual)
        assertEquals("Portal", repo.entries.first().game.franchiseName)
        assertEquals(2, repo.entries.first().game.franchiseOrder)
        assertNotNull(viewModel.uiState.value.savedMessage)
    }
}

private class FakeManualGamesRepository : GamesRepository {
    val entries = mutableListOf<ListEntry>()

    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = flowOf(entries.toList())

    override fun observeAllEntries(): Flow<List<ListEntry>> = flowOf(entries.toList())

    override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry {
        val entry = ListEntry(
            id = "entry-${entries.size + 1}",
            listId = listId,
            game = Game(
                id = "game-${entries.size + 1}",
                name = game.name,
                metacritic = game.metacritic,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
                isManual = game.isManual,
                franchiseName = game.franchiseName,
                franchiseOrder = game.franchiseOrder,
            ),
            status = status,
        )
        entries += entry
        return entry
    }

    override suspend fun removeGameFromList(listId: String, gameId: String) = Unit

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) = Unit

    override suspend fun updateGame(game: Game) = Unit

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
}
