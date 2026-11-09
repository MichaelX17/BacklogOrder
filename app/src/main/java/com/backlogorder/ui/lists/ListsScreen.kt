package com.backlogorder.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.EmptyState
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListsViewModel(
    private val listsRepository: ListsRepository,
) : ViewModel() {
    val lists: StateFlow<List<GameList>> = listsRepository.observeLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createList(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            listsRepository.createList(name.trim())
        }
    }

    fun deleteList(id: String) {
        viewModelScope.launch {
            listsRepository.deleteList(id)
        }
    }
}

@Composable
fun ListsScreen(
    onListClick: (String) -> Unit,
    viewModel: ListsViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return ListsViewModel(container.listsRepository) as T
                    }
                    error("Container not initialized")
                }
            },
        )
    },
) {
    val lists by viewModel.lists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var listToDelete by remember { mutableStateOf<GameList?>(null) }

    BacklogTheme {
        val colors = LocalHudColors.current
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = colors.background,
        ) {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = colors.accent,
                        contentColor = colors.text,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add List")
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "My Custom Lists",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.text,
                        fontWeight = FontWeight.Bold,
                    )

                    if (lists.isEmpty()) {
                        EmptyState(
                            title = "No Lists Yet",
                            message = "Create a custom list using the + button to organize your games.",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(lists, key = { it.id }) { list ->
                                BevelFrame(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onListClick(list.id) },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = list.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = colors.text,
                                            )
                                        }
                                        IconButton(onClick = { listToDelete = list }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete List",
                                                tint = colors.danger,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                containerColor = colors.panel,
                titleContentColor = colors.text,
                textContentColor = colors.text,
                title = { Text("Create New List") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding(),
                    ) {
                        OutlinedTextField(
                            value = newListName,
                            onValueChange = { newListName = it },
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
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.createList(newListName)
                            newListName = ""
                            showCreateDialog = false
                        },
                    ) {
                        Text("Create", color = colors.accent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
            )
        }

        listToDelete?.let { list ->
            AlertDialog(
                onDismissRequest = { listToDelete = null },
                containerColor = colors.panel,
                titleContentColor = colors.text,
                textContentColor = colors.text,
                title = { Text("Delete List") },
                text = { Text("Are you sure you want to delete \"${list.name}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteList(list.id)
                            listToDelete = null
                        },
                    ) {
                        Text("Delete", color = colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listToDelete = null }) {
                        Text("Cancel", color = colors.textMuted)
                    }
                },
            )
        }
    }
}
