package com.backlogorder.ui.games

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun gamesViewModel_deduplicatesGamesInMultipleLists() = runTest {
        val listsFlow = MutableStateFlow(
            listOf(
                GameList("list-1", "Backlog"),
                GameList("list-2", "Favorites"),
            )
        )
        val game = Game(id = "game-1", name = "Devil May Cry 5")
        val entriesFlow = MutableStateFlow(
            listOf(
                ListEntry(id = "e1", listId = "list-1", game = game, status = GameStatus.BACKLOG),
                ListEntry(id = "e2", listId = "list-2", game = game, status = GameStatus.PLAYING),
            )
        )

        val viewModel = GamesViewModel(
            gamesRepository = FakeGamesRepo(entriesFlow),
            listsRepository = FakeListsRepo(listsFlow),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.entries.size)
        assertEquals(1, state.filteredEntries.size)

        val singleItem = state.entries.first()
        assertEquals("Devil May Cry 5", singleItem.game.name)
        assertEquals(listOf("Backlog", "Favorites"), singleItem.listNames)
        assertEquals("Lists: Backlog, Favorites", singleItem.listNamesDisplay)
        assertEquals(GameStatus.PLAYING, singleItem.primaryStatus)
    }
}

private class FakeListsRepo(
    private val flow: Flow<List<GameList>>,
) : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = flow

    override suspend fun createList(name: String): GameList = GameList(id = "1", name = name)

    override suspend fun renameList(id: String, name: String) = Unit

    override suspend fun deleteList(id: String) = Unit
}

private class FakeGamesRepo(
    private val flow: Flow<List<ListEntry>>,
) : GamesRepository {
    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = flow

    override fun observeAllEntries(): Flow<List<ListEntry>> = flow

    override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry {
        error("Not needed")
    }

    override suspend fun removeGameFromList(listId: String, gameId: String) = Unit

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) = Unit

    override suspend fun updateGame(game: Game) = Unit

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
}
