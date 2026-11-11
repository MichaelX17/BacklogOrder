package com.backlogorder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.recommendation.RecommendationEngine
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.domain.score.ScoreCalculator
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.hud.PrimaryButton
import com.backlogorder.ui.hud.ScoreTag
import com.backlogorder.ui.hud.SectionHeader
import com.backlogorder.ui.hud.StatCard
import com.backlogorder.ui.hud.StatusBadge
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private object EmptyListsRepository : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = flowOf(emptyList())

    override suspend fun createList(name: String): GameList = GameList(id = "", name = name)

    override suspend fun renameList(id: String, name: String) = Unit

    override suspend fun deleteList(id: String) = Unit
}

private object EmptyGamesRepository : GamesRepository {
    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = flowOf(emptyList())

    override fun observeAllEntries(): Flow<List<ListEntry>> = flowOf(emptyList())

    override suspend fun addGameToList(
        listId: String,
        game: NewGame,
        status: GameStatus,
    ): ListEntry = error("Not implemented for empty repository")

    override suspend fun removeGameFromList(listId: String, gameId: String) = Unit

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) = Unit

    override suspend fun updateGame(game: Game) = Unit

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) = Unit
}

data class HomeUiState(
    val lists: List<GameList> = emptyList(),
    val allEntries: List<ListEntry> = emptyList(),
    val scoreMap: Map<String, Double?> = emptyMap(),
    val recommendation: ListEntry? = null,
    val upcoming: List<ListEntry> = emptyList(),
    val skipCount: Int = 0,
    val topRankedGames: List<ListEntry> = emptyList(),
) {
    val currentlyPlaying: ListEntry?
        get() = allEntries.firstOrNull { it.status == GameStatus.PLAYING }

    val totalUniqueGames: Int
        get() = allEntries.map { it.game.id }.distinct().size

    val playingCount: Int
        get() = allEntries.filter { it.status == GameStatus.PLAYING }.map { it.game.id }.distinct().size

    val backlogCount: Int
        get() = allEntries.filter { it.status == GameStatus.BACKLOG }.map { it.game.id }.distinct().size

    val completedCount: Int
        get() = allEntries.filter { it.status == GameStatus.COMPLETED }.map { it.game.id }.distinct().size

    val unfinishedCount: Int
        get() = playingCount + backlogCount

    val completionPercentage: Float
        get() = if (totalUniqueGames > 0) (completedCount.toFloat() / totalUniqueGames.toFloat()) else 0f
}

class HomeViewModel(
    private val listsRepository: ListsRepository = EmptyListsRepository,
    private val gamesRepository: GamesRepository = EmptyGamesRepository,
    private val recommendationEngine: RecommendationEngine = RecommendationEngine(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var lastListSnapshot: List<GameList> = emptyList()
    private var lastEntriesSnapshot: List<ListEntry> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                listsRepository.observeLists(),
                gamesRepository.observeAllEntries(),
            ) { lists, entries ->
                val resetSkip = lists != lastListSnapshot || entries != lastEntriesSnapshot
                val effectiveSkip = if (resetSkip) 0 else _uiState.value.skipCount
                lastListSnapshot = lists
                lastEntriesSnapshot = entries

                val scoreMap = entries.associate { entry -> entry.id to ScoreCalculator.computeScore(entry.game) }
                val recommendation = recommendationEngine.findRecommendation(entries, effectiveSkip)
                val upcoming = recommendationEngine.upcoming(entries, effectiveSkip, count = 2)
                val topRankedGames = entries
                    .filter { scoreMap[it.id] != null }
                    .sortedByDescending { scoreMap[it.id] ?: -1.0 }
                    .distinctBy { it.game.id }
                    .take(5)

                _uiState.value = _uiState.value.copy(
                    lists = lists,
                    allEntries = entries,
                    scoreMap = scoreMap,
                    recommendation = recommendation,
                    upcoming = upcoming,
                    skipCount = effectiveSkip,
                    topRankedGames = topRankedGames,
                )
            }.collect { }
        }
    }

    fun onSkipRecommendation() {
        val nextSkip = _uiState.value.skipCount + 1
        updateRecommendation(nextSkip)
    }

    fun startRecommendation(entry: ListEntry) {
        viewModelScope.launch {
            gamesRepository.updateStatus(entry.listId, entry.game.id, GameStatus.PLAYING)
        }
    }

    fun updateRecommendation(skipCount: Int = _uiState.value.skipCount) {
        val entries = _uiState.value.allEntries
        val scoreMap = _uiState.value.scoreMap
        val recommendation = recommendationEngine.findRecommendation(entries, skipCount)
        val upcoming = recommendationEngine.upcoming(entries, skipCount, count = 2)
        val topRankedGames = entries
            .filter { scoreMap[it.id] != null }
            .sortedByDescending { scoreMap[it.id] ?: -1.0 }
            .distinctBy { it.game.id }
            .take(5)
        _uiState.value = _uiState.value.copy(
            recommendation = recommendation,
            upcoming = upcoming,
            skipCount = skipCount,
            topRankedGames = topRankedGames,
        )
    }

    fun refresh() {
        viewModelScope.launch {
            updateRecommendation()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return HomeViewModel(
                            listsRepository = container.listsRepository,
                            gamesRepository = container.gamesRepository,
                        ) as T
                    }
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel() as T
                }
            },
        )
    },
    onListClick: (String) -> Unit = {},
    onGameClick: (String, String) -> Unit = { _, _ -> },
    onSearchClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val colors = LocalHudColors.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        color = colors.background,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.refresh()
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Game Collection Overview",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                        )
                    }
                    Button(
                        onClick = onSearchClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.text,
                        ),
                    ) {
                        Text("Search RAWG")
                    }
                }

                // Currently Playing Hero Card
                SectionHeader("Currently Playing")
                val playingGame = uiState.currentlyPlaying
                if (playingGame != null) {
                    val score = uiState.scoreMap[playingGame.id]
                    val scoreDisplay = ScoreCalculator.scoreDisplay(score, playingGame.game.playtimeHours, true)
                    BevelFrame(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGameClick(playingGame.listId, playingGame.game.id) },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (playingGame.game.backgroundImage != null || playingGame.game.backgroundImageUrl != null) {
                                BevelImage(
                                    game = playingGame.game,
                                    contentDescription = playingGame.game.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playingGame.game.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.text,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    val metaText = buildString {
                                        if (playingGame.game.playtimeHours != null && playingGame.game.playtimeHours > 0) {
                                            append("${playingGame.game.playtimeHours.toInt()}h estimated")
                                        }
                                        if (playingGame.game.metacritic != null) {
                                            if (isNotEmpty()) append(" • ")
                                            append("Metascore ${playingGame.game.metacritic}")
                                        }
                                    }
                                    if (metaText.isNotBlank()) {
                                        Text(
                                            text = metaText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textMuted,
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(status = GameStatus.PLAYING)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ScoreTag(score = score)
                                }
                            }
                        }
                    }
                } else {
                    BevelFrame(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Not currently playing any game",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.text,
                            )
                            Text(
                                text = "Select a game from your backlog or recommendations to start playing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted,
                            )
                        }
                    }
                }

                // Collection Statistics
                SectionHeader("Collection Overview")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatCard(label = "Total Games", value = uiState.totalUniqueGames.toString(), modifier = Modifier.weight(1f))
                    StatCard(label = "Finished", value = uiState.completedCount.toString(), modifier = Modifier.weight(1f))
                    StatCard(label = "Unfinished", value = uiState.unfinishedCount.toString(), modifier = Modifier.weight(1f))
                    StatCard(label = "Lists", value = uiState.lists.size.toString(), modifier = Modifier.weight(1f))
                }

                // Collection Completion Indicator
                BevelFrame(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Library Completion", style = MaterialTheme.typography.titleMedium, color = colors.text)
                            Text(
                                text = "${(uiState.completionPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.accent,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { uiState.completionPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.accent,
                            trackColor = colors.surface,
                        )
                        Text(
                            text = "${uiState.completedCount} finished of ${uiState.totalUniqueGames} total games",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                        )
                    }
                }

                // Top Ranked Games
                if (uiState.topRankedGames.isNotEmpty()) {
                    BevelFrame(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionHeader("Top Prioritized Games")
                            uiState.topRankedGames.forEachIndexed { index, entry ->
                                val score = uiState.scoreMap[entry.id]
                                val scoreDisplay = ScoreCalculator.scoreDisplay(score, entry.game.playtimeHours, true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onGameClick(entry.listId, entry.game.id) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = colors.accent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(32.dp),
                                        )
                                        Column {
                                            Text(
                                                text = entry.game.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = colors.text,
                                            )
                                            Text(
                                                text = "Metascore ${entry.game.metacritic ?: "—"} • ${entry.game.playtimeHours ?: 0.0}h",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.textMuted,
                                            )
                                        }
                                    }
                                    ScoreTag(score = score)
                                }
                            }
                        }
                    }
                }

                // Tonight's Pick Recommendation
                SectionHeader("Tonight's Pick")
                val recommendation = uiState.recommendation
                if (recommendation == null) {
                    EmptyState(
                        title = "No recommendations available",
                        message = "Add games with Metascores and estimated playtime to unlock priority recommendations.",
                    )
                } else {
                    BevelFrame(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Tonight's pick",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.accent,
                                )
                                StatusBadge(status = recommendation.status)
                            }

                            Text(
                                text = recommendation.game.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.text,
                            )

                            val recommendationScore = uiState.scoreMap[recommendation.id]
                            val displayScore = ScoreCalculator.scoreDisplay(recommendationScore, recommendation.game.playtimeHours, true)
                            Text(
                                text = "Score $displayScore • ${recommendation.game.playtimeHours ?: 0.0}h estimated",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    onClick = { viewModel.startRecommendation(recommendation) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                                ) {
                                    Text(
                                        text = "Start",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Button(
                                    onClick = { onGameClick(recommendation.listId, recommendation.game.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.text),
                                ) {
                                    Text(
                                        text = "Continue",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Button(
                                    onClick = viewModel::onSkipRecommendation,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.textMuted),
                                ) {
                                    Text(
                                        text = "Skip",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Lists Summary
                BevelFrame(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Your Custom Lists")
                        if (uiState.lists.isEmpty()) {
                            EmptyState(
                                title = "No custom lists yet",
                                message = "Create your first backlog list to organize your games.",
                            )
                        } else {
                            uiState.lists.forEach { list ->
                                val count = uiState.allEntries.count { it.listId == list.id }
                                BevelFrame(
                                    backgroundColor = colors.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onListClick(list.id) },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = list.name, style = MaterialTheme.typography.titleMedium, color = colors.text)
                                            Text(
                                                text = "$count games",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.textMuted,
                                            )
                                        }
                                        Button(
                                            onClick = { onListClick(list.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.text),
                                        ) {
                                            Text("Open")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
