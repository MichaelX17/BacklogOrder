package com.backlogorder.ui.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.domain.filter.FilterState
import com.backlogorder.domain.filter.GameFilters
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.domain.score.ScoreCalculator
import com.backlogorder.ui.common.FilterSheet
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.hud.HexShape
import com.backlogorder.ui.hud.ScoreTag
import com.backlogorder.ui.hud.StatusBadge
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GameEntryWithListName(
    val entry: ListEntry,
    val listName: String,
)

data class GamesUiState(
    val entries: List<GameEntryWithListName> = emptyList(),
    val filter: FilterState = FilterState(),
    val filteredEntries: List<GameEntryWithListName> = emptyList(),
    val isLoading: Boolean = true,
)

class GamesViewModel(
    gamesRepository: GamesRepository,
    listsRepository: ListsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                gamesRepository.observeAllEntries(),
                listsRepository.observeLists(),
            ) { entries, lists ->
                val listNameMap = lists.associate { it.id to it.name }
                val gameEntries = entries
                    .map { entry -> GameEntryWithListName(entry, listNameMap[entry.listId] ?: "Custom List") }
                    .sortedByDescending { ScoreCalculator.computeScore(it.entry.game) ?: -1.0 }

                val filter = _uiState.value.filter
                val filtered = gameEntries.filter { item ->
                    GameFilters.matches(item.entry, filter)
                }

                _uiState.value = _uiState.value.copy(
                    entries = gameEntries,
                    filteredEntries = filtered,
                    isLoading = false,
                )
            }.collect { }
        }
    }

    fun updateNameFilter(value: String) {
        val newFilter = _uiState.value.filter.copy(name = value)
        applyFilter(newFilter)
    }

    fun updateFilter(newFilter: FilterState) {
        applyFilter(newFilter)
    }

    fun clearFilters() {
        val newFilter = FilterState()
        applyFilter(newFilter)
    }

    fun onStatusToggled(status: GameStatus) {
        val currentStatuses = _uiState.value.filter.statuses.toMutableSet()
        if (!currentStatuses.add(status)) {
            currentStatuses.remove(status)
        }
        applyFilter(_uiState.value.filter.copy(statuses = currentStatuses))
    }

    private fun applyFilter(newFilter: FilterState) {
        val entries = _uiState.value.entries
        val filtered = entries.filter { item ->
            GameFilters.matches(item.entry, newFilter)
        }
        _uiState.value = _uiState.value.copy(
            filter = newFilter,
            filteredEntries = filtered,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onGameClick: (String, String) -> Unit,
    viewModel: GamesViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return GamesViewModel(container.gamesRepository, container.listsRepository) as T
                    }
                    error("Container not initialized")
                }
            },
        )
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val colors = LocalHudColors.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GamesHeader(
                    title = "All Games Collection",
                    searchQuery = uiState.filter.name,
                    onSearchChange = viewModel::updateNameFilter,
                    onFilter = { showFilterSheet = true },
                    isFilterActive = uiState.filter.isActive,
                )

                if (uiState.filteredEntries.isEmpty()) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        EmptyState(
                            title = "No Games Found",
                            message = "No games match your filter or search criteria.",
                        )
                    }
                } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.filteredEntries,
                        key = { "${it.entry.listId}-${it.entry.game.id}" },
                        contentType = { "game_item" },
                    ) { item ->
                        val score = ScoreCalculator.computeScore(item.entry.game)
                        BevelFrame(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGameClick(item.entry.listId, item.entry.game.id) },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                // Cover / Initials
                                if (!item.entry.game.backgroundImage.isNullOrBlank() || !item.entry.game.backgroundImageUrl.isNullOrBlank()) {
                                    BevelImage(
                                        game = item.entry.game,
                                        contentDescription = item.entry.game.name,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                    )
                                } else {
                                    HexShape(modifier = Modifier.size(56.dp), color = colors.accent.copy(alpha = 0.2f)) {
                                        Text(
                                            text = item.entry.game.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                            color = colors.text,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                }

                                // Title & List Name
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = item.entry.game.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "List: ${item.listName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                // Score and Status stacked vertically
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ScoreTag(score = score)
                                    StatusBadge(status = item.entry.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
