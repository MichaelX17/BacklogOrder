package com.backlogorder.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.backlogorder.ui.theme.LocalHudColors
import com.backlogorder.ui.theme.LocalIsDarkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranchiseAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    existingFranchises: List<String>,
    modifier: Modifier = Modifier,
    label: String = "Franchise Name (Optional)",
) {
    val colors = LocalHudColors.current
    val isDark = LocalIsDarkMode.current
    var expanded by remember { mutableStateOf(false) }

    val filteredFranchises = remember(value, existingFranchises) {
        if (value.isBlank()) {
            existingFranchises
        } else {
            existingFranchises.filter { it.contains(value, ignoreCase = true) }
        }
    }

    val fieldContainerColor = if (isDark) colors.background.copy(alpha = 0.6f) else colors.surface.copy(alpha = 0.8f)
    val menuContainerColor = if (isDark) colors.background else colors.surface

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
                    text = label,
                    color = colors.textMuted,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = colors.accent,
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = fieldContainerColor,
                unfocusedContainerColor = fieldContainerColor,
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
                containerColor = menuContainerColor,
                modifier = Modifier.border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            ) {
                filteredFranchises.forEach { franchise ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = colors.accent.copy(alpha = 0.7f),
                            )
                        },
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
