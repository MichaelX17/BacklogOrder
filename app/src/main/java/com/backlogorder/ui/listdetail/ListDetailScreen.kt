package com.backlogorder.ui.listdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.ui.games.GamesHeader
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.domain.score.ScoreCalculator
import com.backlogorder.domain.sort.FranchiseGroup
import com.backlogorder.domain.sort.FranchiseSorter
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.hud.GameCard
import com.backlogorder.ui.hud.PrimaryButton
import com.backlogorder.ui.hud.SectionHeader
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private object EmptyGamesRepository : GamesRepository {
    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = emptyFlow()

    override fun observeAllEntries(): Flow<List<ListEntry>> = emptyFlow()

    override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry =
        ListEntry(
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

private object EmptyListsRepository : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = emptyFlow()

    override suspend fun createList(name: String): GameList = GameList(id = "", name = name)

    override suspend fun renameList(id: String, name: String) = Unit

    override suspend fun deleteList(id: String) = Unit
}

data class FilterState(
    val name: String = "",
    val statuses: Set<GameStatus> = emptySet(),
) {
    val isActive: Boolean get() = name.isNotBlank() || statuses.isNotEmpty()
}

data class ListDetailUiState(
    val listId: String = "",
    val listName: String = "",
    val entries: List<ListEntry> = emptyList(),
    val scoreMap: Map<String, Double?> = emptyMap(),
    val filter: FilterState = FilterState(),
    val filteredEntries: List<ListEntry> = emptyList(),
    val groups: List<FranchiseGroup> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class ListDetailViewModel(
    private val listId: String,
    private val listsRepository: ListsRepository = EmptyListsRepository,
    private val gamesRepository: GamesRepository = EmptyGamesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListDetailUiState(listId = listId))
    val uiState: StateFlow<ListDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                listsRepository.observeLists(),
                gamesRepository.observeEntriesForList(listId),
            ) { lists, entries ->
                val matchingList = lists.firstOrNull { it.id == listId }
                val scoreMap = entries.associate { entry -> entry.id to ScoreCalculator.computeScore(entry.game) }
                val filter = _uiState.value.filter
                val filteredEntries = entries.filter { entry ->
                    val matchesName = filter.name.isBlank() || entry.game.name.contains(filter.name, ignoreCase = true)
                    val matchesStatus = filter.statuses.isEmpty() || entry.status in filter.statuses
                    matchesName && matchesStatus
                }
                val groups = FranchiseSorter.sort(filteredEntries)
                _uiState.value = _uiState.value.copy(
                    listName = matchingList?.name ?: _uiState.value.listName,
                    entries = entries,
                    scoreMap = scoreMap,
                    filteredEntries = filteredEntries,
                    groups = groups,
                    isLoading = false,
                    errorMessage = null,
                )
            }.collect { }
        }
    }

    fun renameList(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                listsRepository.renameList(listId, trimmed)
            }
        }
    }

    fun updateNameFilter(value: String) {
        val newFilter = _uiState.value.filter.copy(name = value)
        val entries = _uiState.value.entries
        val filteredEntries = entries.filter { entry ->
            val matchesName = newFilter.name.isBlank() || entry.game.name.contains(newFilter.name, ignoreCase = true)
            val matchesStatus = newFilter.statuses.isEmpty() || entry.status in newFilter.statuses
            matchesName && matchesStatus
        }
        val groups = FranchiseSorter.sort(filteredEntries)
        _uiState.value = _uiState.value.copy(
            filter = newFilter,
            filteredEntries = filteredEntries,
            groups = groups,
        )
    }

    fun updateFilter(newFilter: FilterState) {
        val entries = _uiState.value.entries
        val filteredEntries = entries.filter { entry ->
            val matchesName = newFilter.name.isBlank() || entry.game.name.contains(newFilter.name, ignoreCase = true)
            val matchesStatus = newFilter.statuses.isEmpty() || entry.status in newFilter.statuses
            matchesName && matchesStatus
        }
        val groups = FranchiseSorter.sort(filteredEntries)
        _uiState.value = _uiState.value.copy(
            filter = newFilter,
            filteredEntries = filteredEntries,
            groups = groups,
        )
    }

    fun clearFilters() {
        val newFilter = FilterState()
        val entries = _uiState.value.entries
        val filteredEntries = entries.filter { entry ->
            val matchesName = newFilter.name.isBlank() || entry.game.name.contains(newFilter.name, ignoreCase = true)
            val matchesStatus = newFilter.statuses.isEmpty() || entry.status in newFilter.statuses
            matchesName && matchesStatus
        }
        val groups = FranchiseSorter.sort(filteredEntries)
        _uiState.value = _uiState.value.copy(
            filter = newFilter,
            filteredEntries = filteredEntries,
            groups = groups,
        )
    }

    fun onStatusToggled(status: GameStatus) {
        val currentStatuses = _uiState.value.filter.statuses.toMutableSet()
        if (!currentStatuses.add(status)) {
            currentStatuses.remove(status)
        }
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(statuses = currentStatuses),
        )
    }
}

@Composable
fun ListDetailScreen(
    listId: String,
    onBack: () -> Unit = {},
    onGameClick: (String, String) -> Unit = { _, _ -> },
    onAddRawgGameClick: (String) -> Unit = {},
    onAddManualGameClick: (String) -> Unit = {},
    viewModel: ListDetailViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return ListDetailViewModel(
                            listId = listId,
                            listsRepository = container.listsRepository,
                            gamesRepository = container.gamesRepository,
                        ) as T
                    }
                    @Suppress("UNCHECKED_CAST")
                    return ListDetailViewModel(listId = listId) as T
                }
            },
        )
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddOptionDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember(uiState.listName) { mutableStateOf(uiState.listName) }

    BacklogTheme {
        val colors = LocalHudColors.current
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GamesHeader(
                    title = uiState.listName.ifBlank { "Custom List" },
                    searchQuery = uiState.filter.name,
                    onSearchChange = viewModel::updateNameFilter,
                    onBack = onBack,
                    onEditTitle = {
                        renameInput = uiState.listName
                        showRenameDialog = true
                    },
                    onAddGame = { showAddOptionDialog = true },
                    onFilter = { showFilterSheet = true },
                    isFilterActive = uiState.filter.isActive,
                )

                if (uiState.groups.isEmpty()) {
                    EmptyState(
                        title = "No Games in this List",
                        message = "Use the Add Game button to add games from RAWG search or manually.",
                    ) {
                        PrimaryButton(
                            text = "Add Game",
                            onClick = { showAddOptionDialog = true },
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(uiState.groups, key = { it.id }) { group ->
                            GroupBlock(
                                group = group,
                                scoreMap = uiState.scoreMap,
                                onGameClick = onGameClick,
                            )
                        }
                    }
                }
            }

            // Rename List Modal Dialog
            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = { Text("Rename List") },
                    text = {
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            label = { Text("List Name", color = colors.textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.textMuted,
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text,
                            ),
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.renameList(renameInput)
                                showRenameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text("Cancel", color = colors.textMuted)
                        }
                    },
                )
            }

            // Modal choice dialog when clicking "Add Game"
            if (showAddOptionDialog) {
                AlertDialog(
                    onDismissRequest = { showAddOptionDialog = false },
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = {
                        Text(
                            text = "Add Game to List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "How would you like to add a game?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted,
                            )

                            Button(
                                onClick = {
                                    showAddOptionDialog = false
                                    onAddRawgGameClick(listId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.text,
                                ),
                            ) {
                                Text(
                                    text = "Search RAWG",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Button(
                                onClick = {
                                    showAddOptionDialog = false
                                    onAddManualGameClick(listId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surface,
                                    contentColor = colors.text,
                                ),
                            ) {
                                Text(
                                    text = "Manual Entry",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    },
                    confirmButton = {},
                )
            }

            if (showFilterSheet) {
                FilterSheet(
                    filter = uiState.filter,
                    onStatusToggle = viewModel::onStatusToggled,
                    onClear = viewModel::clearFilters,
                    onDismiss = { showFilterSheet = false },
                )
            }
        }
    }
}

@Composable
private fun GroupBlock(
    group: FranchiseGroup,
    scoreMap: Map<String, Double?>,
    onGameClick: (String, String) -> Unit,
) {
    val colors = LocalHudColors.current
    BevelFrame(
        backgroundColor = colors.panel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (group.isFranchise) {
                SectionHeader(group.displayName)
            }
            group.entries.forEach { entry ->
                val score = scoreMap[entry.id]
                GameCard(
                    title = entry.game.name,
                    status = entry.status,
                    score = score,
                    meta = "Metascore: ${entry.game.metacritic ?: "—"}\nHours: ${entry.game.playtimeHours ?: 0.0}h",
                    imageUrl = entry.game.backgroundImage,
                    onClick = { onGameClick(entry.listId, entry.game.id) },
                    backgroundColor = colors.surface,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FilterSheet(
    filter: FilterState,
    onStatusToggle: (GameStatus) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalHudColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.panel,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        title = { Text("Filter Games") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Filter by Game Status:", style = MaterialTheme.typography.titleSmall, color = colors.text)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = GameStatus.BACKLOG in filter.statuses,
                            onClick = { onStatusToggle(GameStatus.BACKLOG) },
                            label = { Text(GameStatus.BACKLOG.name) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.warning,
                                selectedLabelColor = colors.text,
                                containerColor = colors.panel,
                                labelColor = colors.textMuted,
                            ),
                        )
                        FilterChip(
                            selected = GameStatus.PLAYING in filter.statuses,
                            onClick = { onStatusToggle(GameStatus.PLAYING) },
                            label = { Text(GameStatus.PLAYING.name) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.success,
                                selectedLabelColor = colors.text,
                                containerColor = colors.panel,
                                labelColor = colors.textMuted,
                            ),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = GameStatus.COMPLETED in filter.statuses,
                            onClick = { onStatusToggle(GameStatus.COMPLETED) },
                            label = { Text(GameStatus.COMPLETED.name) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.text,
                                containerColor = colors.panel,
                                labelColor = colors.textMuted,
                            ),
                        )
                        FilterChip(
                            selected = GameStatus.DROPPED in filter.statuses,
                            onClick = { onStatusToggle(GameStatus.DROPPED) },
                            label = { Text(GameStatus.DROPPED.name) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.danger,
                                selectedLabelColor = colors.text,
                                containerColor = colors.panel,
                                labelColor = colors.textMuted,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Apply", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClear()
                onDismiss()
            }) {
                Text("Clear All", color = colors.danger)
            }
        },
    )
}
