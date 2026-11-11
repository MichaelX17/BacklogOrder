package com.backlogorder.ui.import

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.backlogorder.domain.import.ConflictResolution
import com.backlogorder.domain.import.ImportConflict
import com.backlogorder.domain.import.ImportPlan
import com.backlogorder.ui.theme.LocalHudColors

@Composable
fun ImportConflictDialog(
    plan: ImportPlan,
    resolutions: Map<String, ConflictResolution>,
    onResolutionChanged: (String, ConflictResolution) -> Unit,
    onBulkResolution: (ConflictResolution) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = LocalHudColors.current

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.panel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Resolve Import Conflicts (${plan.conflicts.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { onBulkResolution(ConflictResolution.KEEP_LOCAL) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Keep All Local", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { onBulkResolution(ConflictResolution.KEEP_BACKUP) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                    ) {
                        Text("Keep All Backup", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    plan.conflicts.forEach { conflict ->
                        val currentRes = resolutions[conflict.backupItemId] ?: ConflictResolution.KEEP_LOCAL
                        ConflictCard(
                            conflict = conflict,
                            selectedResolution = currentRes,
                            onSelectResolution = { res -> onResolutionChanged(conflict.backupItemId, res) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = colors.textMuted)
                    }

                    Button(
                        onClick = onApply,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                    ) {
                        Text("Apply Resolved Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: ImportConflict,
    selectedResolution: ConflictResolution,
    onSelectResolution: (ConflictResolution) -> Unit,
) {
    val colors = LocalHudColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val (title, typeLabel) = when (conflict) {
                is ImportConflict.GameMetadata -> Pair(conflict.local.name, "Game Metadata Conflict")
                is ImportConflict.GameRawgId -> Pair(conflict.local.name, "RAWG ID Collision")
                is ImportConflict.ListName -> Pair(conflict.local.name, "List Name Conflict")
                is ImportConflict.Membership -> Pair("List Assignment", "Assignment Status Conflict")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, color = colors.text)
                Text(text = typeLabel, style = MaterialTheme.typography.labelSmall, color = colors.accent)
            }

            when (conflict) {
                is ImportConflict.GameMetadata -> {
                    Text("Local: ${conflict.local.name} (Score: ${conflict.local.rating ?: "—"})", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text("Backup: ${conflict.backup.gameName} (Score: ${conflict.backup.rating ?: "—"})", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }

                is ImportConflict.GameRawgId -> {
                    Text("Local: ${conflict.local.name} (UUID: ${conflict.local.id.take(8)})", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text("Backup: ${conflict.backup.gameName} (UUID: ${conflict.backup.id.toString().take(8)})", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }

                is ImportConflict.ListName -> {
                    Text("Local Name: '${conflict.local.name}'", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text("Backup Name: '${conflict.backup.listName}'", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }

                is ImportConflict.Membership -> {
                    Text("Local Status: ${conflict.local.status}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                    Text("Backup Status: ${conflict.backup.status}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val localLabel = if (conflict is ImportConflict.GameRawgId) "Use Local" else "Keep Local"
                val backupLabel = if (conflict is ImportConflict.GameRawgId) "Use Backup" else "Keep Backup"
                val isLocalSelected = selectedResolution == ConflictResolution.KEEP_LOCAL || selectedResolution == ConflictResolution.USE_LOCAL
                val isBackupSelected = selectedResolution == ConflictResolution.KEEP_BACKUP || selectedResolution == ConflictResolution.USE_BACKUP

                OutlinedButton(
                    onClick = {
                        val res = if (conflict is ImportConflict.GameRawgId) ConflictResolution.USE_LOCAL else ConflictResolution.KEEP_LOCAL
                        onSelectResolution(res)
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (isLocalSelected) ButtonDefaults.outlinedButtonColors(containerColor = colors.accent.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text(localLabel, style = MaterialTheme.typography.labelSmall, color = if (isLocalSelected) colors.accent else colors.text)
                }

                OutlinedButton(
                    onClick = {
                        val res = if (conflict is ImportConflict.GameRawgId) ConflictResolution.USE_BACKUP else ConflictResolution.KEEP_BACKUP
                        onSelectResolution(res)
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (isBackupSelected) ButtonDefaults.outlinedButtonColors(containerColor = colors.accent.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors(),
                ) {
                    Text(backupLabel, style = MaterialTheme.typography.labelSmall, color = if (isBackupSelected) colors.accent else colors.text)
                }
            }
        }
    }
}
