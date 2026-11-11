package com.backlogorder.ui.import

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.import.ImportMode
import com.backlogorder.ui.theme.LocalHudColors

@Composable
fun ImportModeDialog(
    backupData: ExportData,
    onModeSelected: (ImportMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalHudColors.current
    var showRestoreWarning by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "Select Import Mode",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Backup contains ${backupData.lists.size} lists and ${backupData.games.size} games.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { onModeSelected(ImportMode.MERGE) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Merge into my library", fontWeight = FontWeight.Bold)
                            Text("Adds missing items and reconciles conflicts (Recommended)", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    OutlinedButton(
                        onClick = { showRestoreWarning = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Restore — replace library", fontWeight = FontWeight.Bold)
                            Text("Wipes current library and restores backup", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = colors.textMuted)
                    }
                }
            }
        }
    }

    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = { showRestoreWarning = false },
            title = { Text("Confirm Library Restore", fontWeight = FontWeight.Bold, color = colors.danger) },
            text = {
                Text(
                    "This will delete your current library and replace it with the backup. A snapshot will be saved first, but all current lists and games will be gone. This cannot be undone from within the app.",
                    color = colors.text,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreWarning = false
                        onModeSelected(ImportMode.RESTORE)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.danger, contentColor = colors.text),
                ) {
                    Text("Yes, replace my library")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreWarning = false }) {
                    Text("Cancel", color = colors.textMuted)
                }
            },
        )
    }
}
