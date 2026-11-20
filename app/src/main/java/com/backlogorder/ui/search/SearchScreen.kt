package com.backlogorder.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.backlogorder.domain.mapper.RawgMapper
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.model.RawgGame
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.domain.score.ScoreCalculator
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.hud.HexShape
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
            _uiState.value = _uiState.value.copy(selectedGame = game)
            onAddToLists(setOf(listId))
        } else {
            _uiState.value = _uiState.value.copy(selectedGame = game)
        }
    }

    fun onNewListNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(newListName = value)
    }

    fun onAddToLists(listIds: Set<String>, newListName: String? = null) {
        val game = _uiState.value.selectedGame ?: return
        val trimmedNewName = newListName?.trim().orEmpty()
        if (listIds.isEmpty() && trimmedNewName.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAdding = true, errorMessage = null)
            val newGame = RawgMapper.mapRawgGame(game)
            val targetIds = listIds.toMutableSet()

            if (trimmedNewName.isNotBlank()) {
                val created = listsRepository.createList(trimmedNewName)
                targetIds.add(created.id)
            }

            for (listId in targetIds) {
                gamesRepository.addGameToList(listId, newGame, GameStatus.BACKLOG)
            }

            val msg = if (targetIds.size > 1) {
                "Added \"${game.name}\" to ${targetIds.size} lists."
            } else {
                "Added \"${game.name}\" to your list."
            }

            _uiState.value = _uiState.value.copy(
                isAdding = false,
                selectedGame = null,
                newListName = "",
                statusMessage = msg,
            )
        }
    }

    fun dismissGameSelection() {
        _uiState.value = _uiState.value.copy(selectedGame = null, newListName = "")
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedGame = uiState.selectedGame
    val errorText = uiState.errorMessage
    val statusText = uiState.statusMessage

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
                            items(
                                items = uiState.results,
                                key = { it.rawgId },
                                contentType = { "search_result" },
                            ) { game ->
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
                    AddGameToListDialog(
                        game = selectedGame,
                        lists = uiState.lists,
                        newListName = uiState.newListName,
                        isAdding = uiState.isAdding,
                        onNewListNameChanged = viewModel::onNewListNameChanged,
                        onAddToLists = viewModel::onAddToLists,
                        onDismiss = viewModel::dismissGameSelection,
                    )
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
    BevelFrame(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!game.backgroundImage.isNullOrBlank()) {
                BevelImage(
                    model = game.backgroundImage,
                    contentDescription = game.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                HexShape(modifier = Modifier.size(56.dp), color = colors.accent.copy(alpha = 0.2f)) {
                    Text(
                        text = game.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = colors.text,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val metaStr = buildString {
                    val effectiveScore = ScoreCalculator.effectiveMetacritic(game.metacritic, game.rating)
                    if (effectiveScore != null) append("Metacritic: ${effectiveScore.toInt()} • ")
                    if (game.playtimeHours != null && game.playtimeHours > 0) append("${game.playtimeHours.toInt()}h • ")
                    if (game.genres.isNotEmpty()) append(game.genres.take(2).joinToString(", "))
                }.removeSuffix(" • ")
                if (metaStr.isNotBlank()) {
                    Text(
                        text = metaStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddGameToListDialog(
    game: RawgGame,
    lists: List<GameList>,
    newListName: String,
    isAdding: Boolean,
    onNewListNameChanged: (String) -> Unit,
    onAddToLists: (listIds: Set<String>, newListName: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalHudColors.current
    var selectedListIds by remember(lists) {
        mutableStateOf(lists.firstOrNull()?.id?.let { setOf(it) } ?: emptySet())
    }
    var isCreatingNewList by remember(lists) { mutableStateOf(lists.isEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.panel,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        title = {
            Text(
                text = "Add to Lists",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Select lists for \"${game.name}\":",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text,
                )

                if (lists.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        lists.forEach { list ->
                            val isSelected = list.id in selectedListIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        selectedListIds = if (isSelected) {
                                            selectedListIds - list.id
                                        } else {
                                            selectedListIds + list.id
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedListIds = if (checked) {
                                            selectedListIds + list.id
                                        } else {
                                            selectedListIds - list.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.accent,
                                        uncheckedColor = colors.textMuted,
                                    ),
                                )
                                Text(
                                    text = list.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.text,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCreatingNewList) colors.accent.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { isCreatingNewList = !isCreatingNewList }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Checkbox(
                                checked = isCreatingNewList,
                                onCheckedChange = { isCreatingNewList = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colors.accent,
                                    uncheckedColor = colors.textMuted,
                                ),
                            )
                            Text(
                                text = "Create new list...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textMuted,
                            )
                        }
                    }
                }

                if (isCreatingNewList || lists.isEmpty()) {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = onNewListNameChanged,
                        label = { Text("New List Name", color = colors.textMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.textMuted.copy(alpha = 0.35f),
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                        ),
                    )
                }
            }
        },
        confirmButton = {
            val canConfirm = !isAdding && (selectedListIds.isNotEmpty() || (isCreatingNewList && newListName.isNotBlank()))
            TextButton(
                enabled = canConfirm,
                onClick = {
                    onAddToLists(
                        selectedListIds,
                        if (isCreatingNewList) newListName else null,
                    )
                },
            ) {
                Text(
                    text = if (isAdding) "Adding..." else "Add",
                    color = colors.accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = colors.textMuted,
                )
            }
        },
    )
}
