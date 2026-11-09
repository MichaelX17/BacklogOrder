package com.backlogorder.ui.search

import androidx.compose.ui.test.junit4.createComposeRule
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.model.RawgGame
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun debounceEmitsOncePerSettledQuery() = runTest {
        val results = mutableListOf<String>()
        val viewModel = SearchViewModel(
            apiKeyProvider = InMemoryApiKeyProvider("key-1"),
            listsRepository = FakeListsRepository(MutableStateFlow(emptyList())),
            gamesRepository = FakeGamesRepository(MutableStateFlow(emptyList())),
            searcher = { query, _ ->
                results += query
                listOf(RawgGame(rawgId = 1, name = query))
            },
        )

        viewModel.onQueryChanged("zelda")
        advanceUntilIdle()
        assertEquals(listOf("zelda"), results)

        viewModel.onQueryChanged("portal")
        advanceUntilIdle()
        assertEquals(listOf("zelda", "portal"), results)
    }

    @Test
    fun addFlowAddsGameToSelectedList() = runTest {
        val entriesFlow = MutableStateFlow<List<ListEntry>>(emptyList())
        val viewModel = SearchViewModel(
            apiKeyProvider = InMemoryApiKeyProvider("key-1"),
            listsRepository = FakeListsRepository(MutableStateFlow(listOf(GameList("list-1", "Favorites")))),
            gamesRepository = FakeGamesRepository(entriesFlow),
            searcher = { _, _ -> emptyList() },
        )

        viewModel.onGameSelected(RawgGame(rawgId = 99, name = "Portal"))
        viewModel.onAddToList("list-1")
        advanceUntilIdle()

        assertEquals(1, entriesFlow.value.size)
        assertEquals("Portal", entriesFlow.value.first().game.name)
        assertEquals(GameStatus.BACKLOG, entriesFlow.value.first().status)
    }

    @Test
    fun limitedModeShowsCtaWithoutApiKey() = runTest {
        val viewModel = SearchViewModel(
            apiKeyProvider = InMemoryApiKeyProvider(null),
            listsRepository = FakeListsRepository(MutableStateFlow(emptyList())),
            gamesRepository = FakeGamesRepository(MutableStateFlow(emptyList())),
            searcher = { _, _ -> listOf(RawgGame(rawgId = 1, name = "Ignored")) },
        )

        viewModel.onQueryChanged("zelda")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.hasApiKey)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}

private class FakeListsRepository(
    private val listsFlow: MutableStateFlow<List<GameList>>,
) : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = listsFlow

    override suspend fun createList(name: String): GameList {
        val list = GameList(id = "list-${listsFlow.value.size + 1}", name = name)
        listsFlow.value = listsFlow.value + list
        return list
    }

    override suspend fun renameList(id: String, name: String) = Unit

    override suspend fun deleteList(id: String) = Unit
}

private class FakeGamesRepository(
    private val entriesFlow: MutableStateFlow<List<ListEntry>>,
) : GamesRepository {
    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = entriesFlow

    override fun observeAllEntries(): Flow<List<ListEntry>> = entriesFlow

    override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry {
        val entry = ListEntry(
            id = "entry-${entriesFlow.value.size + 1}",
            listId = listId,
            game = Game(
                id = game.rawgId?.toString() ?: "manual-${entriesFlow.value.size + 1}",
                name = game.name,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
            ),
            status = status,
        )
        entriesFlow.value = entriesFlow.value + entry
        return entry
    }

    override suspend fun removeGameFromList(listId: String, gameId: String) = Unit

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) = Unit

    override suspend fun updateGame(game: Game) = Unit

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
}
