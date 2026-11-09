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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.domain.score.ScoreCalculator
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.hud.HexShape
import com.backlogorder.ui.hud.ScoreTag
import com.backlogorder.ui.hud.StatusBadge
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GameEntryWithListName(
    val entry: ListEntry,
    val listName: String,
)

class GamesViewModel(
    gamesRepository: GamesRepository,
    listsRepository: ListsRepository,
) : ViewModel() {
    val entries: StateFlow<List<GameEntryWithListName>> = combine(
        gamesRepository.observeAllEntries(),
        listsRepository.observeLists(),
    ) { entries, lists ->
        val listNameMap = lists.associate { it.id to it.name }
        entries
            .map { entry -> GameEntryWithListName(entry, listNameMap[entry.listId] ?: "Custom List") }
            .sortedByDescending { ScoreCalculator.computeScore(it.entry.game) ?: -1.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

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
    val items by viewModel.entries.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { it.entry.game.name.contains(searchQuery, ignoreCase = true) }
        }
    }

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
                    title = "All Games Collection",
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                )

                if (filteredItems.isEmpty()) {
                    EmptyState(
                        title = "No Games Found",
                        message = "Add games via Search or Manual Entry to populate your collection.",
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filteredItems, key = { "${it.entry.listId}-${it.entry.game.id}" }) { item ->
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
                                    if (!item.entry.game.backgroundImage.isNullOrBlank()) {
                                        BevelImage(
                                            model = item.entry.game.backgroundImage!!,
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
}
