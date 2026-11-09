package com.backlogorder.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.model.RawgGame
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.hud.PrimaryButton
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private object EmptyListsRepository : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = emptyFlow()

    override suspend fun createList(name: String): GameList = GameList(id = "", name = name)

    override suspend fun renameList(id: String, name: String) = Unit

    override suspend fun deleteList(id: String) = Unit
}

private object EmptyGamesRepository : GamesRepository {
    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = emptyFlow()

    override fun observeAllEntries(): Flow<List<ListEntry>> = emptyFlow()

    override suspend fun addGameToList(
        listId: String,
        game: NewGame,
        status: GameStatus,
    ): ListEntry = ListEntry(
        id = "temp",
        listId = listId,
        game = Game(
            id = game.rawgId?.toString() ?: "manual-${System.currentTimeMillis()}",
            name = game.name,
            backgroundImage = game.backgroundImage,
            metacritic = game.metacritic,
            rating = game.rating,
            playtimeHours = game.playtimeHours,
            genres = game.genres,
            platforms = game.platforms,
            tags = game.tags,
        ),
        status = status,
    )

    override suspend fun removeGameFromList(listId: String, gameId: String) = Unit

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) = Unit

    override suspend fun updateGame(game: Game) = Unit

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
}

class InMemoryApiKeyProvider(initialKey: String? = null) : ApiKeyProvider {
    private val state = MutableStateFlow(initialKey)
    val currentValue: String? get() = state.value

    override val key: Flow<String?> = state

    override val currentKey: String? get() = state.value

    override suspend fun setKey(value: String) {
        state.value = value
    }

    override suspend fun clear() {
        state.value = null
    }
}

data class SearchUiState(
    val targetListId: String? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val results: List<RawgGame> = emptyList(),
    val lists: List<GameList> = emptyList(),
    val hasApiKey: Boolean = false,
    val selectedGame: RawgGame? = null,
    val newListName: String = "",
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    val hasResults: Boolean get() = results.isNotEmpty()
}

class SearchViewModel(
    private val apiKeyProvider: ApiKeyProvider = InMemoryApiKeyProvider(),
    private val listsRepository: ListsRepository = EmptyListsRepository,
    private val gamesRepository: GamesRepository = EmptyGamesRepository,
    private val searcher: suspend (String, String) -> List<RawgGame> = { _, _ -> emptyList() },
    private val targetListId: String? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState(targetListId = targetListId))
    val uiState: StateFlow<SearchUiState> = _uiState

    private var currentApiKey: String? = null
    private var searchJob: Job? = null

    init {
        currentApiKey = (apiKeyProvider as? InMemoryApiKeyProvider)?.currentValue
        viewModelScope.launch {
            apiKeyProvider.key.collect { key ->
                currentApiKey = key
                _uiState.value = _uiState.value.copy(
                    hasApiKey = !key.isNullOrBlank(),
                )
                if (key.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
                }
            }
        }

        viewModelScope.launch {
            listsRepository.observeLists().collect { lists ->
                _uiState.value = _uiState.value.copy(lists = lists)
            }
        }
    }

    fun onQueryChanged(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val trimmed = value.trim()
            val key = currentApiKey
            if (trimmed.isEmpty()) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
                return@launch
            }
            if (key.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = searcher(trimmed, key)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = result,
                    statusMessage = if (result.isEmpty()) "No matching games found." else null,
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Search failed.",
                )
            }
        }
    }

    fun onGameSelected(game: RawgGame) {
        val listId = _uiState.value.targetListId
        if (!listId.isNullOrBlank()) {
            addGameToList(listId, game)
        } else {
            _uiState.value = _uiState.value.copy(selectedGame = game)
        }
    }

    fun onAddToList(listId: String) {
        val game = _uiState.value.selectedGame ?: return
        addGameToList(listId, game)
    }

    fun onNewListNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(newListName = value)
    }

    fun createListAndAddSelected() {
        val name = _uiState.value.newListName.trim()
        val game = _uiState.value.selectedGame ?: return
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "List name is required.")
            return
        }

        viewModelScope.launch {
            val created = listsRepository.createList(name)
            addGameToList(created.id, game)
            _uiState.value = _uiState.value.copy(newListName = "")
        }
    }

    private fun addGameToList(listId: String, game: RawgGame) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAdding = true, errorMessage = null)
            val newGame = NewGame(
                rawgId = game.rawgId,
                name = game.name,
                backgroundImage = game.backgroundImage,
                metacritic = game.metacritic,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
                genres = game.genres,
                platforms = game.platforms,
                tags = game.tags,
            )
            gamesRepository.addGameToList(listId, newGame, GameStatus.BACKLOG)
            _uiState.value = _uiState.value.copy(
                isAdding = false,
                selectedGame = null,
                statusMessage = "Added \"${game.name}\" to your list.",
            )
        }
    }
}

@Composable
fun SearchScreen(
    listId: String? = null,
    viewModel: SearchViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return SearchViewModel(
                            apiKeyProvider = container.apiKeyProvider,
                            listsRepository = container.listsRepository,
                            gamesRepository = container.gamesRepository,
                            searcher = { query, key -> container.rawgRemoteDataSource.searchGames(query, key) },
                            targetListId = listId,
                        ) as T
                    }
                    @Suppress("UNCHECKED_CAST")
                    return SearchViewModel(targetListId = listId) as T
                }
            },
        )
    },
    onBack: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedGame = uiState.selectedGame
    val errorText = uiState.errorMessage
    val statusText = uiState.statusMessage

    BacklogTheme {
        val colors = LocalHudColors.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = colors.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    com.backlogorder.ui.hud.BackIconButton(onClick = onBack)
                    Text(
                        text = "Search RAWG",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.text,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (!uiState.hasApiKey) {
                    BevelFrame(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Add your RAWG API key to search for games.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.text,
                            )
                            PrimaryButton(text = "Go to onboarding", onClick = onNavigateToOnboarding)
                        }
                    }
                    return@Surface
                }

                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChanged,
                    placeholder = { Text("Search games...", color = colors.textMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.textMuted.copy(alpha = 0.35f),
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                    ),
                )

                if (statusText != null) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.success,
                    )
                }

                when {
                    uiState.isLoading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(color = colors.accent)
                        }
                    }

                    errorText != null -> {
                        Text(text = errorText, color = colors.danger)
                    }

                    uiState.hasResults -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(uiState.results, key = { it.rawgId }) { game ->
                                SearchResultRow(
                                    game = game,
                                    onClick = { viewModel.onGameSelected(game) },
                                )
                            }
                        }
                    }

                    else -> {
                        EmptyState(
                            title = "Search RAWG",
                            message = "Type a game title above to search the RAWG database.",
                        )
                    }
                }

                if (selectedGame != null && uiState.targetListId == null) {
                    BevelFrame(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Add \"${selectedGame.name}\" to a list:",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.text,
                            )
                            if (uiState.lists.isEmpty()) {
                                OutlinedTextField(
                                    value = uiState.newListName,
                                    onValueChange = viewModel::onNewListNameChanged,
                                    label = { Text("New list name", color = colors.textMuted) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                PrimaryButton(
                                    text = if (uiState.isAdding) "Adding..." else "Create list and add game",
                                    enabled = !uiState.isAdding && uiState.newListName.isNotBlank(),
                                    onClick = viewModel::createListAndAddSelected,
                                )
                            } else {
                                uiState.lists.forEach { list ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.onAddToList(list.id) }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(list.name, color = colors.text)
                                        TextButton(onClick = { viewModel.onAddToList(list.id) }) {
                                            Text("Add", color = colors.accent)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = uiState.newListName,
                                    onValueChange = viewModel::onNewListNameChanged,
                                    label = { Text("Or create a new list", color = colors.textMuted) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                PrimaryButton(
                                    text = if (uiState.isAdding) "Adding..." else "Create list and add",
                                    enabled = !uiState.isAdding && uiState.newListName.isNotBlank(),
                                    onClick = viewModel::createListAndAddSelected,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    game: RawgGame,
    onClick: () -> Unit,
) {
    val colors = LocalHudColors.current
    BevelFrame(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            game.backgroundImage?.let { url ->
                BevelImage(
                    model = url,
                    contentDescription = game.name,
                    modifier = Modifier
                        .height(50.dp)
                        .padding(end = 4.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = game.name, style = MaterialTheme.typography.titleMedium, color = colors.text)
                val metaStr = buildString {
                    if (game.metacritic != null) append("Metacritic: ${game.metacritic} • ")
                    if (game.playtimeHours != null && game.playtimeHours > 0) append("${game.playtimeHours.toInt()}h • ")
                    if (game.genres.isNotEmpty()) append(game.genres.take(2).joinToString(", "))
                }.removeSuffix(" • ")
                if (metaStr.isNotBlank()) {
                    Text(text = metaStr, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }
        }
    }
}
