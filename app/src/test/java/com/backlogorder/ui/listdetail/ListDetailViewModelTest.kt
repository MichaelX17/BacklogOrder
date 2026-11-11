package com.backlogorder.ui.listdetail

import com.backlogorder.domain.filter.FilterState
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun filtersEntriesByNameAndStatus() = runTest {
        val entriesFlow = MutableStateFlow(
            listOf(
                ListEntry(
                    id = "1",
                    listId = "list-1",
                    game = Game(id = "g-1", name = "Zelda", metacritic = 90, playtimeHours = 8.0),
                    status = GameStatus.PLAYING,
                ),
                ListEntry(
                    id = "2",
                    listId = "list-1",
                    game = Game(id = "g-2", name = "Portal", metacritic = 88, playtimeHours = 5.0),
                    status = GameStatus.BACKLOG,
                ),
            ),
        )
        val listsFlow = MutableStateFlow(listOf(GameList(id = "list-1", name = "Favorites")))
        val viewModel = ListDetailViewModel(
            listId = "list-1",
            listsRepository = FakeListsRepository(listsFlow),
            gamesRepository = FakeGamesRepository(entriesFlow),
        )

        advanceUntilIdle()
        viewModel.updateFilter(
            FilterState(
                name = "zel",
                statuses = setOf(GameStatus.PLAYING),
            ),
        )

        assertEquals(listOf("Zelda"), viewModel.uiState.value.filteredEntries.map { it.game.name })
    }

    @Test
    fun groupsEntriesByFranchiseWithStandaloneLast() = runTest {
        val entriesFlow = MutableStateFlow(
            listOf(
                ListEntry(
                    id = "1",
                    listId = "list-1",
                    game = Game(
                        id = "g-1",
                        name = "Alpha",
                        franchiseName = "Legend",
                        franchiseOrder = 2,
                        metacritic = 90,
                        playtimeHours = 5.0,
                    ),
                    status = GameStatus.BACKLOG,
                ),
                ListEntry(
                    id = "2",
                    listId = "list-1",
                    game = Game(
                        id = "g-2",
                        name = "Beta",
                        franchiseName = "Legend",
                        franchiseOrder = 1,
                        metacritic = 70,
                        playtimeHours = 8.0,
                    ),
                    status = GameStatus.PLAYING,
                ),
                ListEntry(
                    id = "3",
                    listId = "list-1",
                    game = Game(
                        id = "g-3",
                        name = "Gamma",
                        franchiseName = "Pixel",
                        metacritic = 95,
                        playtimeHours = 2.0,
                    ),
                    status = GameStatus.BACKLOG,
                ),
                ListEntry(
                    id = "4",
                    listId = "list-1",
                    game = Game(
                        id = "g-4",
                        name = "Delta",
                        metacritic = 60,
                        playtimeHours = 4.0,
                    ),
                    status = GameStatus.PLAYING,
                ),
            ),
        )
        val viewModel = ListDetailViewModel(
            listId = "list-1",
            listsRepository = FakeListsRepository(MutableStateFlow(listOf(GameList(id = "list-1", name = "Favorites")))),
            gamesRepository = FakeGamesRepository(entriesFlow),
        )

        advanceUntilIdle()

        assertEquals(
            listOf("Pixel", "Legend", "Delta"),
            viewModel.uiState.value.groups.map { it.displayName },
        )
    }

    @Test
    fun clearFiltersResetsToDefaultState() = runTest {
        val viewModel = ListDetailViewModel(
            listId = "list-1",
            listsRepository = FakeListsRepository(MutableStateFlow(listOf(GameList(id = "list-1", name = "Favorites")))),
            gamesRepository = FakeGamesRepository(MutableStateFlow(emptyList())),
        )

        advanceUntilIdle()
        viewModel.updateNameFilter("zel")
        viewModel.onStatusToggled(GameStatus.BACKLOG)
        viewModel.clearFilters()

        assertEquals(FilterState(), viewModel.uiState.value.filter)
    }

    @Test
    fun filterShowsEntireGroupWhenAtLeastOneGameMatchesStatus() = runTest {
        val entriesFlow = MutableStateFlow(
            listOf(
                ListEntry(
                    id = "1",
                    listId = "list-1",
                    game = Game(id = "g-1", name = "Zelda 1", franchiseName = "Zelda"),
                    status = GameStatus.COMPLETED,
                ),
                ListEntry(
                    id = "2",
                    listId = "list-1",
                    game = Game(id = "g-2", name = "Zelda 2", franchiseName = "Zelda"),
                    status = GameStatus.PLAYING,
                ),
            ),
        )
        val viewModel = ListDetailViewModel(
            listId = "list-1",
            listsRepository = FakeListsRepository(MutableStateFlow(listOf(GameList(id = "list-1", name = "Favorites")))),
            gamesRepository = FakeGamesRepository(entriesFlow),
        )

        advanceUntilIdle()
        viewModel.onStatusToggled(GameStatus.PLAYING)

        assertEquals(1, viewModel.uiState.value.groups.size)
        assertEquals(2, viewModel.uiState.value.groups[0].entries.size)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

private class FakeListsRepository(
    private val listsFlow: MutableStateFlow<List<GameList>>,
) : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = listsFlow

    override suspend fun createList(name: String): GameList = GameList(id = "list-${listsFlow.value.size + 1}", name = name)

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
                metacritic = game.metacritic,
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
