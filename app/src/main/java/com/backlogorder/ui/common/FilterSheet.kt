package com.backlogorder.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.backlogorder.domain.filter.FilterState
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.ui.theme.LocalHudColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
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
