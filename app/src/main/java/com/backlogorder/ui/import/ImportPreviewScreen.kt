package com.backlogorder.ui.import

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.import.ImportMode
import com.backlogorder.domain.import.ImportPlan
import com.backlogorder.ui.theme.LocalHudColors

@Composable
fun ImportPreviewDialog(
    plan: ImportPlan,
    mode: ImportMode,
    backupData: ExportData,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = LocalHudColors.current
    val isRestore = mode == ImportMode.RESTORE

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.panel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (isRestore) "Restore Preview" else "Merge Preview",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                )

                if (!isRestore && plan.isNoOp) {
                    Text(
                        text = "Already up to date — nothing to import.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.text,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "All lists, games, and list assignments in this backup match your local library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                        ) {
                            Text("Close")
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (isRestore) {
                                Text("• ${backupData.lists.size} lists will be restored", color = colors.text)
                                Text("• ${backupData.games.size} games will be restored", color = colors.text)
                                Text("• ${backupData.listGames.size} list assignments will be restored", color = colors.text)
                            } else {
                                Text("• ${plan.toAddLists.size} lists to add", color = colors.text)
                                Text("• ${plan.toAddGames.size} games to add", color = colors.text)
                                Text("• ${plan.toAddListGames.size} assignments to add", color = colors.text)
                                if (plan.conflicts.isNotEmpty()) {
                                    Text("• ${plan.conflicts.size} conflicts to resolve", color = colors.accent, fontWeight = FontWeight.Bold)
                                }
                                Text("• ${plan.skippedLists} lists, ${plan.skippedGames} games already up to date", color = colors.textMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (plan.missingCoversSkipped > 0) {
                        Text(
                            text = "Warning: ${plan.missingCoversSkipped} cover image files referenced in the backup are missing on this device. Remote URLs will be used where available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.danger,
                        )
                    }

                    Text(
                        text = "Note: A safety snapshot of your current library will be saved to cache before any database changes.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    ) {
                        TextButton(onClick = onCancel) {
                            Text("Cancel", color = colors.textMuted)
                        }

                        val actionText = when {
                            isRestore -> "Restore Library"
                            plan.conflicts.isNotEmpty() -> "Review ${plan.conflicts.size} Conflicts"
                            else -> "Import ${plan.toAddLists.size + plan.toAddGames.size + plan.toAddListGames.size} Items"
                        }

                        Button(
                            onClick = onProceed,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                        ) {
                            Text(actionText)
                        }
                    }
                }
            }
        }
    }
}
