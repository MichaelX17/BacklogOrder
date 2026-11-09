package com.backlogorder.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun recommendationSkipAndResetOnDataChange() = runTest {
        val listsFlow = MutableStateFlow(listOf(GameList("list-1", "My backlog")))
        val entriesFlow = MutableStateFlow(
            listOf(
                ListEntry(
                    id = "entry-1",
                    listId = "list-1",
                    game = Game(
                        id = "game-1",
                        name = "Alpha",
                        metacritic = 80,
                        rating = 4.0,
                        playtimeHours = 2.0,
                    ),
                    status = GameStatus.BACKLOG,
                ),
                ListEntry(
                    id = "entry-2",
                    listId = "list-1",
                    game = Game(
                        id = "game-2",
                        name = "Bravo",
                        metacritic = 90,
                        rating = 5.0,
                        playtimeHours = 1.0,
                    ),
                    status = GameStatus.PLAYING,
                ),
            ),
        )
        val viewModel = HomeViewModel(
            listsRepository = FakeListsRepository(listsFlow),
            gamesRepository = FakeGamesRepository(entriesFlow),
        )

        advanceUntilIdle()
        assertEquals("entry-2", viewModel.uiState.value.recommendation?.id)

        viewModel.onSkipRecommendation()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.skipCount)
        assertEquals("entry-1", viewModel.uiState.value.recommendation?.id)

        entriesFlow.value += ListEntry(
            id = "entry-3",
            listId = "list-1",
            game = Game(
                id = "game-3",
                name = "Charlie",
                metacritic = 85,
                rating = 4.5,
                playtimeHours = 1.5,
            ),
            status = GameStatus.PLAYING,
        )
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.skipCount)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateShowsHelpfulMessage() {
        val viewModel = HomeViewModel(
            listsRepository = FakeListsRepository(MutableStateFlow(emptyList())),
            gamesRepository = FakeGamesRepository(MutableStateFlow(emptyList())),
        )
        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel)
        }

        composeTestRule.onAllNodesWithText("No recommendations available").onFirst().assertExists()
    }

    @Test
    fun contentStateShowsRecommendation() {
        val listFlow = MutableStateFlow(listOf(GameList("list-1", "My list")))
        val entriesFlow = MutableStateFlow(
            listOf(
                ListEntry(
                    id = "entry-1",
                    listId = "list-1",
                    game = Game(id = "game-1", name = "Alpha", metacritic = 80, rating = 4.0, playtimeHours = 2.0),
                    status = GameStatus.BACKLOG,
                ),
            ),
        )
        val viewModel = HomeViewModel(
            listsRepository = FakeListsRepository(listFlow),
            gamesRepository = FakeGamesRepository(entriesFlow),
        )

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel)
        }

        composeTestRule.onAllNodesWithText("Alpha").onFirst().assertExists()
    }
}

private class FakeListsRepository(
    private val listsFlow: MutableStateFlow<List<GameList>>,
) : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = listsFlow

    override suspend fun createList(name: String): GameList {
        val newList = GameList(id = "list-${listsFlow.value.size + 1}", name = name)
        listsFlow.value += newList
        return newList
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
        val listEntry = ListEntry(
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
        entriesFlow.value += listEntry
        return listEntry
    }

    override suspend fun removeGameFromList(listId: String, gameId: String) {
        entriesFlow.value = entriesFlow.value.filterNot { it.listId == listId && it.game.id == gameId }
    }

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) {
        entriesFlow.value = entriesFlow.value.map {
            if (it.listId == listId && it.game.id == gameId) it.copy(status = status) else it
        }
    }

    override suspend fun updateGame(game: Game) {
        entriesFlow.value = entriesFlow.value.map {
            if (it.game.id == game.id) it.copy(game = game) else it
        }
    }

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
}
