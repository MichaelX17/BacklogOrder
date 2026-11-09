package com.backlogorder.ui.gamedetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.data.remote.RawgRemoteDataSource
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.score.ScoreCalculator
import com.backlogorder.ui.common.InteractiveCropDialog
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.HexShape
import com.backlogorder.ui.hud.ScoreTag
import com.backlogorder.ui.hud.StatusBadge
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

data class GameDetailUiState(
    val listId: String = "",
    val gameId: String = "",
    val entry: ListEntry? = null,
    val scoreText: String = "—",
    val franchiseName: String = "",
    val franchiseOrder: String = "",
    val removeConfirmVisible: Boolean = false,
    val isReloadingCover: Boolean = false,
    val coverReloadError: String? = null,
) {
    val scoreValue: Double?
        get() = entry?.let { ScoreCalculator.computeScore(it.game) }
}

class GameDetailViewModel(
    private val listId: String,
    private val gameId: String,
    private val gamesRepository: GamesRepository = EmptyGamesRepository,
    private val rawgRemoteDataSource: RawgRemoteDataSource? = null,
    private val apiKeyProvider: ApiKeyProvider? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameDetailUiState(listId = listId, gameId = gameId))
    val uiState: StateFlow<GameDetailUiState> = _uiState

    val existingFranchises: StateFlow<List<String>> = gamesRepository.observeAllEntries()
        .map { entries ->
            entries.mapNotNull { it.game.franchiseName }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val franchiseGames: StateFlow<List<ListEntry>> = combine(
        _uiState,
        gamesRepository.observeAllEntries(),
    ) { state, allEntries ->
        val currentFranchise = state.entry?.game?.franchiseName
        if (currentFranchise.isNullOrBlank()) {
            emptyList()
        } else {
            allEntries
                .filter { it.game.franchiseName.equals(currentFranchise, ignoreCase = true) }
                .distinctBy { it.game.id }
                .sortedBy { it.game.franchiseOrder ?: Int.MAX_VALUE }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            gamesRepository.observeEntriesForList(listId).collect { entries ->
                val entry = entries.firstOrNull { it.game.id == gameId }
                _uiState.value = _uiState.value.copy(
                    entry = entry,
                    franchiseName = entry?.game?.franchiseName ?: "",
                    franchiseOrder = entry?.game?.franchiseOrder?.toString() ?: "",
                    scoreText = entry?.game?.let { game ->
                        ScoreCalculator.scoreDisplay(
                            ScoreCalculator.computeScore(game),
                            game.playtimeHours,
                            true,
                        )
                    } ?: "—",
                )
            }
        }
    }

    fun onStatusChanged(status: GameStatus) {
        val entry = _uiState.value.entry ?: return
        if (entry.status == status) {
            return
        }
        viewModelScope.launch {
            gamesRepository.updateStatus(listId, gameId, status)
        }
    }

    fun updateGameCover(imagePath: String?) {
        val currentEntry = _uiState.value.entry ?: return
        val updatedGame = currentEntry.game.copy(
            backgroundImage = imagePath,
            updatedAt = System.currentTimeMillis(),
        )
        val updatedEntry = currentEntry.copy(game = updatedGame)
        _uiState.value = _uiState.value.copy(entry = updatedEntry)

        viewModelScope.launch {
            gamesRepository.updateGame(updatedGame)
        }
    }

    fun updateManualGameDetails(
        name: String,
        metacritic: Int?,
        rating: Double?,
        playtimeHours: Double?,
    ) {
        val currentEntry = _uiState.value.entry ?: return
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val updatedGame = currentEntry.game.copy(
            name = trimmedName,
            metacritic = metacritic,
            rating = rating,
            playtimeHours = playtimeHours,
            updatedAt = System.currentTimeMillis(),
        )
        val updatedEntry = currentEntry.copy(game = updatedGame)
        _uiState.value = _uiState.value.copy(entry = updatedEntry)

        viewModelScope.launch {
            gamesRepository.updateGame(updatedGame)
        }
    }

    fun reloadRawgCover() {
        val currentEntry = _uiState.value.entry ?: return
        val rawgId = currentEntry.game.rawgId ?: return
        val dataSource = rawgRemoteDataSource ?: return
        val keyProvider = apiKeyProvider ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReloadingCover = true, coverReloadError = null)
            try {
                val apiKey = keyProvider.currentKey ?: ""
                val rawgDetails = dataSource.getGameDetails(rawgId, apiKey)
                rawgDetails.backgroundImage?.let { rawgUrl ->
                    updateGameCover(rawgUrl)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("GameDetailViewModel", "Failed to reload RAWG cover", e)
                _uiState.value = _uiState.value.copy(coverReloadError = e.localizedMessage ?: "Failed to reload cover")
            } finally {
                _uiState.value = _uiState.value.copy(isReloadingCover = false)
            }
        }
    }

    fun onFranchiseNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(franchiseName = value)
    }

    fun onFranchiseOrderChanged(value: String) {
        _uiState.value = _uiState.value.copy(franchiseOrder = value)
    }

    fun saveFranchise() {
        val trimmedName = _uiState.value.franchiseName.trim()
        val order = _uiState.value.franchiseOrder.trim().toIntOrNull()
        viewModelScope.launch {
            gamesRepository.setFranchise(gameId, trimmedName.ifEmpty { null }, order)
        }
    }

    fun clearFranchise() {
        _uiState.value = _uiState.value.copy(franchiseName = "", franchiseOrder = "")
        viewModelScope.launch {
            gamesRepository.setFranchise(gameId, null, null)
        }
    }

    fun requestRemove() {
        _uiState.value = _uiState.value.copy(removeConfirmVisible = true)
    }

    fun cancelRemove() {
        _uiState.value = _uiState.value.copy(removeConfirmVisible = false)
    }

    fun confirmRemove() {
        viewModelScope.launch {
            gamesRepository.removeGameFromList(listId, gameId)
            _uiState.value = _uiState.value.copy(removeConfirmVisible = false, entry = null)
        }
    }
}

private suspend fun saveImageToDevicePictures(context: Context, model: Any, name: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val request = ImageRequest.Builder(context)
            .data(model)
            .build()
        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.image as? coil3.BitmapImage)?.bitmap
                ?: (result.image as? BitmapDrawable)?.bitmap
                ?: return@withContext false

            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "BacklogOrder").apply { if (!exists()) mkdirs() }
            val cleanName = name.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val file = File(appDir, "${cleanName}_cover_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            return@withContext true
        }
        false
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }
}

@Composable
fun GameDetailScreen(
    listId: String,
    gameId: String,
    onBack: () -> Unit = {},
    onRemoved: () -> Unit = {},
    viewModel: GameDetailViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return GameDetailViewModel(
                            listId = listId,
                            gameId = gameId,
                            gamesRepository = container.gamesRepository,
                            rawgRemoteDataSource = container.rawgRemoteDataSource,
                            apiKeyProvider = container.apiKeyProvider,
                        ) as T
                    }
                    @Suppress("UNCHECKED_CAST")
                    return GameDetailViewModel(listId = listId, gameId = gameId) as T
                }
            },
        )
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingFranchises by viewModel.existingFranchises.collectAsState()
    val franchiseGames by viewModel.franchiseGames.collectAsState()
    val entry = uiState.entry
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showCoverDialog by remember { mutableStateOf(false) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var showFranchiseDialog by remember { mutableStateOf(false) }
    var showFranchiseListDialog by remember { mutableStateOf(false) }
    var showEditManualGameDialog by remember { mutableStateOf(false) }

    var editManualNameInput by remember(entry) { mutableStateOf(entry?.game?.name ?: "") }
    var editManualMetacriticInput by remember(entry) { mutableStateOf(entry?.game?.metacritic?.toString() ?: "") }
    var editManualRatingInput by remember(entry) { mutableStateOf(entry?.game?.rating?.toString() ?: "") }
    var editManualPlaytimeInput by remember(entry) { mutableStateOf(entry?.game?.playtimeHours?.toString() ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            pendingCropUri = uri
        }
    }

    BacklogTheme {
        val colors = LocalHudColors.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = colors.background,
        ) {
            if (entry == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Game not found", style = MaterialTheme.typography.headlineSmall, color = colors.text)
                        Spacer(modifier = Modifier.height(12.dp))
                        com.backlogorder.ui.hud.BackIconButton(onClick = onBack)
                    }
                }
                return@Surface
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    com.backlogorder.ui.hud.BackIconButton(onClick = onBack)
                    StatusBadge(status = entry.status)
                }

                // Row composition: Game Cover on LEFT side next to Game Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showCoverDialog = true },
                    ) {
                        if (!entry.game.backgroundImage.isNullOrBlank()) {
                            BevelImage(
                                model = entry.game.backgroundImage!!,
                                contentDescription = "${entry.game.name} cover",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            HexShape(
                                modifier = Modifier.fillMaxSize(),
                                color = colors.accent.copy(alpha = 0.2f),
                            ) {
                                Text(
                                    text = entry.game.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colors.text,
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = if (entry.game.isManual) {
                                Modifier.clickable {
                                    editManualNameInput = entry.game.name
                                    editManualMetacriticInput = entry.game.metacritic?.toString() ?: ""
                                    editManualRatingInput = entry.game.rating?.toString() ?: ""
                                    editManualPlaytimeInput = entry.game.playtimeHours?.toString() ?: ""
                                    showEditManualGameDialog = true
                                }
                            } else {
                                Modifier
                            },
                        ) {
                            Text(
                                text = entry.game.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.text,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (entry.game.isManual) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Manual Game",
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ScoreTag(score = uiState.scoreValue)
                            com.backlogorder.ui.hud.TimeTag(playtimeHours = entry.game.playtimeHours)
                        }
                    }
                }

                BevelFrame(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Status", style = MaterialTheme.typography.titleMedium, color = colors.text)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusButton(
                                    status = GameStatus.BACKLOG,
                                    isSelected = entry.status == GameStatus.BACKLOG,
                                    onClick = { viewModel.onStatusChanged(GameStatus.BACKLOG) },
                                    modifier = Modifier.weight(1f),
                                )
                                StatusButton(
                                    status = GameStatus.PLAYING,
                                    isSelected = entry.status == GameStatus.PLAYING,
                                    onClick = { viewModel.onStatusChanged(GameStatus.PLAYING) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StatusButton(
                                    status = GameStatus.COMPLETED,
                                    isSelected = entry.status == GameStatus.COMPLETED,
                                    onClick = { viewModel.onStatusChanged(GameStatus.COMPLETED) },
                                    modifier = Modifier.weight(1f),
                                )
                                StatusButton(
                                    status = GameStatus.DROPPED,
                                    isSelected = entry.status == GameStatus.DROPPED,
                                    onClick = { viewModel.onStatusChanged(GameStatus.DROPPED) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // 1. Game Insights Section (3 Equal Columns with Material 3 Icons)
                BevelFrame(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Game Insights",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MetricColumnCard(
                                icon = Icons.Default.BarChart,
                                value = "${entry.game.metacritic ?: "—"}",
                                label = "Metascore",
                                modifier = Modifier.weight(1f),
                            )
                            MetricColumnCard(
                                icon = Icons.Default.StarHalf,
                                value = "${entry.game.rating ?: "—"}",
                                label = "RAWG Rating",
                                modifier = Modifier.weight(1f),
                            )
                            MetricColumnCard(
                                icon = Icons.Default.Alarm,
                                value = if (entry.game.playtimeHours != null) "${entry.game.playtimeHours}h" else "—",
                                label = "Est. Playtime",
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (entry.game.genres.isNotEmpty() || entry.game.platforms.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (entry.game.genres.isNotEmpty()) {
                                    Text(
                                        text = "Genres: ${entry.game.genres.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted,
                                    )
                                }
                                if (entry.game.platforms.isNotEmpty()) {
                                    Text(
                                        text = "Platforms: ${entry.game.platforms.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted,
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Franchise Hub Section
                BevelFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!entry.game.franchiseName.isNullOrBlank()) {
                                showFranchiseListDialog = true
                            } else {
                                showFranchiseDialog = true
                            }
                        },
                ) {
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
                                text = "Franchise Hub",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.text,
                                fontWeight = FontWeight.Bold,
                            )

                            // Edit Bubble Button (Only tapping this button opens Edit Modal)
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showFranchiseDialog = true },
                                color = colors.accent.copy(alpha = 0.18f),
                                shape = CircleShape,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Franchise",
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f, fill = false),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsEsports,
                                            contentDescription = "Franchise",
                                            tint = colors.accent,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    Text(
                                        text = entry.game.franchiseName ?: "Standalone Game",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.text,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                if (entry.game.franchiseOrder != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            text = "#${entry.game.franchiseOrder}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colors.text,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = viewModel::requestRemove,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger, contentColor = colors.text),
                ) {
                    Text("Remove from list")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Franchise Games List Modal Dialog (Displays when tapping Franchise Hub Card)
            if (showFranchiseListDialog && !entry.game.franchiseName.isNullOrBlank()) {
                AlertDialog(
                    onDismissRequest = { showFranchiseListDialog = false },
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = { Text("${entry.game.franchiseName} Franchise Collection") },
                    text = {
                        if (franchiseGames.isEmpty()) {
                            Text("No games found in this franchise.", color = colors.textMuted)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(franchiseGames, key = { "${it.listId}-${it.game.id}" }) { franchiseEntry ->
                                    val itemScore = ScoreCalculator.computeScore(franchiseEntry.game)
                                    BevelFrame(
                                        backgroundColor = colors.surface,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            // Cover on Left
                                            if (!franchiseEntry.game.backgroundImage.isNullOrBlank()) {
                                                BevelImage(
                                                    model = franchiseEntry.game.backgroundImage!!,
                                                    contentDescription = franchiseEntry.game.name,
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(RoundedCornerShape(10.dp)),
                                                )
                                            } else {
                                                HexShape(
                                                    modifier = Modifier.size(56.dp),
                                                    color = colors.accent.copy(alpha = 0.2f),
                                                ) {
                                                    Text(
                                                        text = franchiseEntry.game.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = colors.text,
                                                    )
                                                }
                                            }

                                            // Game Title on Top, 2x1 Status & Score Bubbles Row below
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                ) {
                                                    Text(
                                                        text = franchiseEntry.game.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.text,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false),
                                                    )
                                                    if (franchiseEntry.game.franchiseOrder != null) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(colors.accent)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                                        ) {
                                                            Text(
                                                                text = "#${franchiseEntry.game.franchiseOrder}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = colors.text,
                                                            )
                                                        }
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    StatusBadge(status = franchiseEntry.status, modifier = Modifier.weight(1f))
                                                    ScoreTag(score = itemScore, modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFranchiseListDialog = false }) {
                            Text("Close", color = colors.accent)
                        }
                    },
                )
            }

            // Edit Manual Game Details Modal Dialog
            if (showEditManualGameDialog) {
                AlertDialog(
                    onDismissRequest = { showEditManualGameDialog = false },
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = { Text("Edit Manual Game Data") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .imePadding(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Game Title", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                OutlinedTextField(
                                    value = editManualNameInput,
                                    onValueChange = { editManualNameInput = it },
                                    singleLine = true,
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
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Metacritic Score (0-100)", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                OutlinedTextField(
                                    value = editManualMetacriticInput,
                                    onValueChange = { editManualMetacriticInput = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
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
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("User Rating (0-5)", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                OutlinedTextField(
                                    value = editManualRatingInput,
                                    onValueChange = { editManualRatingInput = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
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
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Playtime Hours", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                OutlinedTextField(
                                    value = editManualPlaytimeInput,
                                    onValueChange = { editManualPlaytimeInput = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
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
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.updateManualGameDetails(
                                    name = editManualNameInput,
                                    metacritic = editManualMetacriticInput.trim().toIntOrNull(),
                                    rating = editManualRatingInput.trim().toDoubleOrNull(),
                                    playtimeHours = editManualPlaytimeInput.trim().toDoubleOrNull(),
                                )
                                showEditManualGameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                        ) {
                            Text("Save Changes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditManualGameDialog = false }) {
                            Text("Cancel", color = colors.textMuted)
                        }
                    },
                )
            }

            // Edit Franchise Modal Dialog
            if (showFranchiseDialog) {
                AlertDialog(
                    onDismissRequest = { showFranchiseDialog = false },
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = { Text("Edit Franchise Information") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .imePadding(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            FranchiseAutocompleteField(
                                value = uiState.franchiseName,
                                onValueChange = viewModel::onFranchiseNameChanged,
                                existingFranchises = existingFranchises,
                            )
                            OutlinedTextField(
                                value = uiState.franchiseOrder,
                                onValueChange = viewModel::onFranchiseOrderChanged,
                                label = { Text("Franchise Order", color = colors.textMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
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
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.saveFranchise()
                                showFranchiseDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    viewModel.clearFranchise()
                                    showFranchiseDialog = false
                                },
                            ) {
                                Text("Clear", color = colors.danger)
                            }
                            TextButton(onClick = { showFranchiseDialog = false }) {
                                Text("Cancel", color = colors.textMuted)
                            }
                        }
                    },
                )
            }

            // Full Cover View & Edit Modal Dialog
            if (showCoverDialog) {
                AlertDialog(
                    onDismissRequest = { showCoverDialog = false },
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = { Text("Game Cover Options") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (!entry.game.backgroundImage.isNullOrBlank()) {
                                BevelImage(
                                    model = entry.game.backgroundImage!!,
                                    contentDescription = entry.game.name,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                )
                            } else {
                                HexShape(
                                    modifier = Modifier.size(160.dp),
                                    color = colors.accent.copy(alpha = 0.2f),
                                ) {
                                    Text(
                                        text = entry.game.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                        style = MaterialTheme.typography.displayMedium,
                                        color = colors.text,
                                    )
                                }
                            }

                            if (uiState.coverReloadError != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = colors.danger.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = uiState.coverReloadError!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.danger,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(
                                            onClick = {
                                                viewModel.reloadRawgCover()
                                            },
                                        ) {
                                            Text("Retry", color = colors.accent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                                ) {
                                    Text("Edit Cover (Interactive 1x1 Crop)")
                                }

                                if (entry.game.rawgId != null) {
                                    Button(
                                        onClick = {
                                            viewModel.reloadRawgCover()
                                            showCoverDialog = false
                                        },
                                        enabled = !uiState.isReloadingCover,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.text),
                                    ) {
                                        Text(if (uiState.isReloadingCover) "Reloading..." else "Refresh / Reload RAWG Cover")
                                    }
                                }

                                if (!entry.game.backgroundImage.isNullOrBlank()) {
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val success = saveImageToDevicePictures(context, entry.game.backgroundImage!!, entry.game.name)
                                                if (success) {
                                                    Toast.makeText(context, "Saved cover to Pictures/BacklogOrder", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Failed to save cover", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("Save Cover to Device Pictures", color = colors.text)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCoverDialog = false }) {
                            Text("Close", color = colors.textMuted)
                        }
                    },
                )
            }

            if (pendingCropUri != null) {
                InteractiveCropDialog(
                    imageUri = pendingCropUri!!,
                    onDismiss = { pendingCropUri = null },
                    onCropConfirmed = { croppedPath ->
                        viewModel.updateGameCover(croppedPath)
                        pendingCropUri = null
                        showCoverDialog = false
                    },
                )
            }

            if (uiState.removeConfirmVisible) {
                AlertDialog(
                    onDismissRequest = viewModel::cancelRemove,
                    containerColor = colors.panel,
                    titleContentColor = colors.text,
                    textContentColor = colors.text,
                    title = { Text("Remove Game") },
                    text = { Text("Are you sure you want to remove \"${entry.game.name}\" from this list?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.confirmRemove()
                                onRemoved()
                            },
                        ) {
                            Text("Remove", color = colors.danger)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::cancelRemove) {
                            Text("Cancel", color = colors.textMuted)
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FranchiseAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    existingFranchises: List<String>,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    var expanded by remember { mutableStateOf(false) }

    val filteredFranchises = remember(value, existingFranchises) {
        if (value.isBlank()) {
            existingFranchises
        } else {
            existingFranchises.filter { it.contains(value, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredFranchises.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text("Franchise Name (Optional)", color = colors.textMuted) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
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

        if (filteredFranchises.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.panel,
            ) {
                filteredFranchises.forEach { franchise ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = franchise,
                                color = colors.text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        onClick = {
                            onValueChange(franchise)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricColumnCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.accent,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusButton(
    status: GameStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    val tint = when (status) {
        GameStatus.BACKLOG -> colors.warning
        GameStatus.PLAYING -> colors.success
        GameStatus.COMPLETED -> colors.accent
        GameStatus.DROPPED -> colors.danger
    }

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) tint else colors.surface,
            contentColor = if (isSelected) colors.text else colors.textMuted,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
