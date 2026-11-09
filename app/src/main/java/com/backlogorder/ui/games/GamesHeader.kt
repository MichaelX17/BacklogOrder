package com.backlogorder.ui.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors

/**
 * Modern Material 3 header for Games and List Detail screens.
 *
 * Section A: Horizontal row with optional Back button, Title with optional Edit action, and optional Add Game button.
 * Section B: Horizontal row with Search text field and optional Filter button.
 */
@Composable
fun GamesHeader(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onEditTitle: (() -> Unit)? = null,
    onAddGame: (() -> Unit)? = null,
    onFilter: (() -> Unit)? = null,
    isFilterActive: Boolean = false,
) {
    val colors = LocalHudColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Section A: Main navigation and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // A. Back navigation icon button
            if (onBack != null) {
                com.backlogorder.ui.hud.BackIconButton(onClick = onBack)
            }

            // B. Screen title and edit action
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onEditTitle != null) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onEditTitle)
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        } else {
                            Modifier
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                if (onEditTitle != null) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit title",
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // C. Add Game action button
            if (onAddGame != null) {
                Button(
                    onClick = onAddGame,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White,
                        )
                        Text(
                            text = "Add Game",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Section B: Search and filter controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // A. Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = (if (onFilter != null) Modifier.weight(1f) else Modifier.fillMaxWidth())
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Search games...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textMuted,
                        modifier = Modifier.size(20.dp),
                    )
                },
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

            // B. Filter action button
            if (onFilter != null) {
                OutlinedButton(
                    onClick = onFilter,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isFilterActive) colors.accent else colors.textMuted.copy(alpha = 0.35f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFilterActive) colors.accent.copy(alpha = 0.15f) else colors.surface,
                        contentColor = if (isFilterActive) colors.accent else colors.text,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            modifier = Modifier.size(20.dp),
                            tint = if (isFilterActive) colors.accent else colors.text,
                        )
                        Text(
                            text = if (isFilterActive) "Filter •" else "Filter",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (isFilterActive) colors.accent else colors.text,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111027)
@Composable
private fun GamesHeaderPreview() {
    BacklogTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            GamesHeader(
                title = "Favorites",
                searchQuery = "",
                onSearchChange = {},
                onBack = {},
                onEditTitle = {},
                onAddGame = {},
                onFilter = {},
                isFilterActive = false,
            )
        }
    }
}
