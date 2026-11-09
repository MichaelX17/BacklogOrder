package com.backlogorder.ui.manualgame

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.ui.common.InteractiveCropDialog
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.BevelImage
import com.backlogorder.ui.hud.PrimaryButton
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private object EmptyGamesRepository : GamesRepository {
    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> = emptyFlow()

    override fun observeAllEntries(): Flow<List<ListEntry>> = emptyFlow()

    override suspend fun addGameToList(
        listId: String,
        game: NewGame,
        status: GameStatus,
    ): ListEntry = ListEntry(
        id = "manual-${System.nanoTime()}",
        listId = listId,
        game = Game(
            id = game.rawgId?.toString() ?: "manual-${System.nanoTime()}",
            name = game.name,
            backgroundImage = game.backgroundImage,
            metacritic = game.metacritic,
            rating = game.rating,
            playtimeHours = game.playtimeHours,
            franchiseName = game.franchiseName,
            franchiseOrder = game.franchiseOrder,
            isManual = game.isManual,
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

data class ManualGameUiState(
    val listId: String? = null,
    val name: String = "",
    val backgroundImage: String? = null,
    val metacritic: String = "",
    val rating: String = "",
    val playtimeHours: String = "",
    val franchiseName: String = "",
    val franchiseOrder: String = "",
    val lists: List<GameList> = emptyList(),
    val newListName: String = "",
    val errors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val savedMessage: String? = null,
) {
    val nameError: String? get() = errors["name"]
    val metacriticError: String? get() = errors["metacritic"]
    val ratingError: String? get() = errors["rating"]
    val playtimeHoursError: String? get() = errors["playtimeHours"]
    val franchiseOrderError: String? get() = errors["franchiseOrder"]
    val listIdError: String? get() = errors["listId"]
}

class ManualGameViewModel(
    private val gamesRepository: GamesRepository = EmptyGamesRepository,
    private val listsRepository: ListsRepository = EmptyListsRepository,
    private val listId: String? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManualGameUiState(listId = listId))
    val uiState: StateFlow<ManualGameUiState> = _uiState

    val existingFranchises: StateFlow<List<String>> = gamesRepository.observeAllEntries()
        .map { entries ->
            entries.mapNotNull { it.game.franchiseName }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            listsRepository.observeLists().collect { lists ->
                val activeListId = _uiState.value.listId ?: lists.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    lists = lists,
                    listId = activeListId,
                )
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errors = _uiState.value.errors - "name")
    }

    fun onBackgroundImageChanged(uriString: String?) {
        _uiState.value = _uiState.value.copy(backgroundImage = uriString)
    }

    fun onMetacriticChanged(value: String) {
        _uiState.value = _uiState.value.copy(metacritic = value, errors = _uiState.value.errors - "metacritic")
    }

    fun onRatingChanged(value: String) {
        _uiState.value = _uiState.value.copy(rating = value, errors = _uiState.value.errors - "rating")
    }

    fun onPlaytimeHoursChanged(value: String) {
        _uiState.value = _uiState.value.copy(playtimeHours = value, errors = _uiState.value.errors - "playtimeHours")
    }

    fun onFranchiseNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(franchiseName = value)
    }

    fun onFranchiseOrderChanged(value: String) {
        _uiState.value = _uiState.value.copy(franchiseOrder = value, errors = _uiState.value.errors - "franchiseOrder")
    }

    fun onNewListNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(newListName = value, errors = _uiState.value.errors - "listId")
    }

    fun submit() {
        val current = _uiState.value
        val errors = validate(current)
        if (errors.isNotEmpty()) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true, errors = emptyMap())

            var targetListId = current.listId
            if (targetListId.isNullOrBlank() && current.newListName.isNotBlank()) {
                val created = listsRepository.createList(current.newListName.trim())
                targetListId = created.id
            }

            if (targetListId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errors = mapOf("listId" to "A list is required before adding a game."),
                )
                return@launch
            }

            val newGame = NewGame(
                name = current.name.trim(),
                backgroundImage = current.backgroundImage,
                metacritic = current.metacritic.trim().takeIf { it.isNotEmpty() }?.toIntOrNull(),
                rating = current.rating.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
                playtimeHours = current.playtimeHours.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
                isManual = true,
                franchiseName = current.franchiseName.trim().takeIf { it.isNotEmpty() },
                franchiseOrder = current.franchiseOrder.trim().takeIf { it.isNotEmpty() }?.toIntOrNull(),
            )

            gamesRepository.addGameToList(targetListId, newGame, GameStatus.BACKLOG)
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                savedMessage = "Added ${newGame.name} to your list.",
            )
        }
    }

    private fun validate(state: ManualGameUiState): Map<String, String> {
        val errors = linkedMapOf<String, String>()
        if (state.listId.isNullOrBlank() && state.newListName.isBlank()) {
            errors["listId"] = "Select an existing list or enter a new list name."
        }

        val name = state.name.trim()
        if (name.isBlank()) {
            errors["name"] = "Game title is required."
        }

        val metacriticText = state.metacritic.trim()
        if (metacriticText.isNotEmpty()) {
            val metacritic = metacriticText.toIntOrNull()
            if (metacritic == null || metacritic !in 0..100) {
                errors["metacritic"] = "Metacritic must be a number from 0 to 100."
            }
        }

        val ratingText = state.rating.trim()
        if (ratingText.isNotEmpty()) {
            val rating = ratingText.toDoubleOrNull()
            if (rating == null || rating !in 0.0..5.0) {
                errors["rating"] = "Rating must be between 0 and 5."
            }
        }

        val playtimeText = state.playtimeHours.trim()
        if (playtimeText.isNotEmpty()) {
            val playtime = playtimeText.toDoubleOrNull()
            if (playtime == null || playtime < 0.0) {
                errors["playtimeHours"] = "Playtime must be 0 or greater."
            }
        }

        val franchiseOrderText = state.franchiseOrder.trim()
        if (franchiseOrderText.isNotEmpty()) {
            val order = franchiseOrderText.toIntOrNull()
            if (order == null || order < 0) {
                errors["franchiseOrder"] = "Franchise order must be a non-negative integer."
            }
        }

        return errors
    }
}

@Composable
fun ManualGameScreen(
    listId: String? = null,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: ManualGameViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return ManualGameViewModel(
                            gamesRepository = container.gamesRepository,
                            listsRepository = container.listsRepository,
                            listId = listId,
                        ) as T
                    }
                    @Suppress("UNCHECKED_CAST")
                    return ManualGameViewModel(listId = listId) as T
                }
            },
        )
    },
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingFranchises by viewModel.existingFranchises.collectAsState()
    val listIdError = uiState.listIdError
    val savedMessage = uiState.savedMessage
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            pendingCropUri = uri
        }
    }

    LaunchedEffect(savedMessage) {
        if (savedMessage != null) {
            onSaved()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    com.backlogorder.ui.hud.BackIconButton(onClick = onBack)

                    Text(
                        text = "Add Game Manually",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.text,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (listIdError != null) {
                    Text(text = listIdError, color = colors.danger, style = MaterialTheme.typography.bodySmall)
                }

                if (uiState.listId == null) {
                    BevelFrame(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Create a new list for this game:", style = MaterialTheme.typography.titleMedium, color = colors.text)
                            OutlinedTextField(
                                value = uiState.newListName,
                                onValueChange = viewModel::onNewListNameChanged,
                                label = {
                                    Text(
                                        text = "List Name",
                                        color = colors.textMuted,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
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
                }

                // 1x1 Interactive Cover Image Selector
                BevelFrame(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Game Cover Image (Optional 1x1)",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                        )
                        if (!uiState.backgroundImage.isNullOrBlank()) {
                            BevelImage(
                                model = uiState.backgroundImage!!,
                                contentDescription = "Cover Preview",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    },
                                ) {
                                    Text("Change Cover", color = colors.accent)
                                }
                                TextButton(onClick = { viewModel.onBackgroundImageChanged(null) }) {
                                    Text("Remove", color = colors.danger)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surface)
                                    .border(1.dp, colors.textMuted.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("+", style = MaterialTheme.typography.headlineLarge, color = colors.accent)
                                    Text("Select Cover", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                                }
                            }
                        }
                    }
                }

                BevelFrame(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        LabeledField(
                            label = "Game Title",
                            value = uiState.name,
                            onValueChange = viewModel::onNameChanged,
                            isError = uiState.nameError != null,
                            error = uiState.nameError,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LabeledField(
                                label = "Metacritic (0-100)",
                                value = uiState.metacritic,
                                onValueChange = viewModel::onMetacriticChanged,
                                isError = uiState.metacriticError != null,
                                error = uiState.metacriticError,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f),
                            )
                            LabeledField(
                                label = "User Rating (0-5)",
                                value = uiState.rating,
                                onValueChange = viewModel::onRatingChanged,
                                isError = uiState.ratingError != null,
                                error = uiState.ratingError,
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LabeledField(
                                label = "Playtime (Hours)",
                                value = uiState.playtimeHours,
                                onValueChange = viewModel::onPlaytimeHoursChanged,
                                isError = uiState.playtimeHoursError != null,
                                error = uiState.playtimeHoursError,
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f),
                            )
                            LabeledField(
                                label = "Franchise Order",
                                value = uiState.franchiseOrder,
                                onValueChange = viewModel::onFranchiseOrderChanged,
                                isError = uiState.franchiseOrderError != null,
                                error = uiState.franchiseOrderError,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        FranchiseAutocompleteField(
                            value = uiState.franchiseName,
                            onValueChange = viewModel::onFranchiseNameChanged,
                            existingFranchises = existingFranchises,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = if (uiState.isSubmitting) "Saving..." else "Add Game To List",
                    onClick = viewModel::submit,
                    enabled = !uiState.isSubmitting,
                )

                if (savedMessage != null) {
                    Text(text = savedMessage, color = colors.success)
                }
            }
        }

        if (pendingCropUri != null) {
            InteractiveCropDialog(
                imageUri = pendingCropUri!!,
                onDismiss = { pendingCropUri = null },
                onCropConfirmed = { croppedPath ->
                    viewModel.onBackgroundImageChanged(croppedPath)
                    pendingCropUri = null
                },
            )
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
            label = {
                Text(
                    text = "Franchise Name (Optional)",
                    color = colors.textMuted,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryEditable),
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
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    color = colors.textMuted,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
        if (error != null) {
            Text(text = error, color = colors.danger, style = MaterialTheme.typography.bodySmall)
        }
    }
}
