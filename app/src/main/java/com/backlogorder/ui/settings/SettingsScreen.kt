package com.backlogorder.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.domain.model.ThemeId
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.SettingsRepository
import com.backlogorder.ui.hud.PrimaryButton
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import com.backlogorder.ui.theme.VioletPrimary
import com.backlogorder.ui.theme.parseHexColor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val apiKeyProvider: ApiKeyProvider,
) : ViewModel() {
    val apiKey: StateFlow<String?> = apiKeyProvider.key
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeId: StateFlow<ThemeId> = settingsRepository.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeId.VIOLET)

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val customColorHex: StateFlow<String> = settingsRepository.customColorHex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "7045F5")

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            apiKeyProvider.setKey(key)
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            apiKeyProvider.clear()
        }
    }

    fun setThemeId(themeId: ThemeId) {
        viewModelScope.launch {
            settingsRepository.setTheme(themeId)
        }
    }

    fun setDarkMode(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(value)
        }
    }

    fun setCustomColorHex(hex: String) {
        viewModelScope.launch {
            settingsRepository.setCustomColorHex(hex)
        }
    }

    fun exportData(context: android.content.Context) {
        viewModelScope.launch {
            val result = settingsRepository.exportData()
            result.getOrNull()?.let { exportData ->
                settingsRepository.shareExportedData(exportData).getOrNull()?.let { uri ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share BacklogOrder Export").apply {
                        if (context !is Activity) {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = run {
        val context = LocalContext.current.applicationContext
        val container = (context as? BacklogOrderApplication)?.container
        viewModel(
            factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (container != null) {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(
                            settingsRepository = container.settingsRepository,
                            apiKeyProvider = container.apiKeyProvider,
                        ) as T
                    }
                    error("Container not initialized")
                }
            },
        )
    },
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val themeId by viewModel.themeId.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val customColorHex by viewModel.customColorHex.collectAsState()

    var apiKeyInput by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    BacklogTheme(themeId = themeId, darkTheme = isDarkMode, customColorHex = customColorHex) {
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                )

                // 1. Theme & Appearance Palette Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.panel),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        // Dark/Light Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dark Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.text,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (isDarkMode) "App is using Dark Theme" else "App is using Light Theme",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                )
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = viewModel::setDarkMode,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.text,
                                    checkedTrackColor = colors.accent,
                                    uncheckedThumbColor = colors.textMuted,
                                    uncheckedTrackColor = colors.surface,
                                ),
                            )
                        }

                        // Unified Appearance Palette Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Appearance Palette",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.text,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (themeId == ThemeId.CUSTOM) "#${customColorHex.uppercase()}" else themeId.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.accent,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // Swatches Row
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ThemeId.entries.forEach { id ->
                                    val isSelected = themeId == id
                                    val swatchColor = when (id) {
                                        ThemeId.VIOLET -> VioletPrimary
                                        ThemeId.EMERALD -> Color(0xFF10B981)
                                        ThemeId.CRIMSON -> Color(0xFFEF4444)
                                        ThemeId.CUSTOM -> parseHexColor(customColorHex)
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) colors.text else colors.textMuted.copy(alpha = 0.3f),
                                                    shape = CircleShape,
                                                )
                                                .clickable { viewModel.setThemeId(id) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.text),
                                                )
                                            }
                                        }
                                        Text(
                                            text = id.name.lowercase().replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) colors.text else colors.textMuted,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }

                            // Integrated Rainbow Spectrum Slider
                            var currentHue by remember(customColorHex) {
                                val parsed = parseHexColor(customColorHex)
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(
                                    android.graphics.Color.argb(
                                        (parsed.alpha * 255).toInt(),
                                        (parsed.red * 255).toInt(),
                                        (parsed.green * 255).toInt(),
                                        (parsed.blue * 255).toInt(),
                                    ),
                                    hsv,
                                )
                                mutableFloatStateOf(hsv[0])
                            }

                            RainbowHueSlider(
                                hue = currentHue,
                                selectedColor = parseHexColor(customColorHex),
                                onHueChange = { newHue ->
                                    currentHue = newHue
                                    val rgbInt = android.graphics.Color.HSVToColor(floatArrayOf(newHue, 0.85f, 0.95f))
                                    val r = (rgbInt shrink 16) and 0xFF
                                    val g = (rgbInt shrink 8) and 0xFF
                                    val b = rgbInt and 0xFF
                                    val newHex = String.format("%02X%02X%02X", r, g, b)
                                    if (themeId != ThemeId.CUSTOM) {
                                        viewModel.setThemeId(ThemeId.CUSTOM)
                                    }
                                    viewModel.setCustomColorHex(newHex)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // 2. RAWG API Connection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.panel),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "RAWG API Connection",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (apiKey.isNullOrBlank()) "No API key stored" else "Connected key: ••••${apiKey?.takeLast(4)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Update API Key", color = colors.textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface,
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.textMuted.copy(alpha = 0.35f),
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text,
                            ),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    if (apiKeyInput.isNotBlank()) {
                                        viewModel.saveApiKey(apiKeyInput.trim())
                                        apiKeyInput = ""
                                        saveMessage = "API Key saved successfully."
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.text),
                            ) {
                                Text("Save Key")
                            }
                            if (!apiKey.isNullOrBlank()) {
                                TextButton(
                                    onClick = {
                                        viewModel.clearApiKey()
                                        saveMessage = "API Key cleared."
                                    },
                                    modifier = Modifier.weight(0.8f),
                                ) {
                                    Text("Clear", color = colors.danger)
                                }
                            }
                        }
                        saveMessage?.let { msg ->
                            Text(text = msg, color = colors.success, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // 3. Backup & Data Management Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.panel),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Backup & Data Management",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Export your game library and custom lists to JSON backup.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                        val context = LocalContext.current
                        PrimaryButton(
                            text = "Export Collection JSON",
                            onClick = { viewModel.exportData(context) },
                        )
                    }
                }

                // 4. Application Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.panel),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "BacklogOrder v1.0",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.text,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Personal video game collection manager & prioritization system.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RainbowHueSlider(
    hue: Float,
    selectedColor: Color,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spectrumColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red,
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { constraints.maxWidth.toFloat() }
        val thumbRadiusDp = 14.dp
        val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }
        val trackHeightDp = 20.dp

        val activeWidthPx = (widthPx - 2 * thumbRadiusPx).coerceAtLeast(1f)
        val normalizedHue = (hue % 360f + 360f) % 360f
        val thumbOffsetPx = thumbRadiusPx + (normalizedHue / 360f) * activeWidthPx

        fun updateHueFromX(xPx: Float) {
            val clampedX = (xPx - thumbRadiusPx).coerceIn(0f, activeWidthPx)
            val newHue = (clampedX / activeWidthPx) * 360f
            onHueChange(newHue)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        updateHueFromX(offset.x)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        updateHueFromX(change.position.x)
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            // Horizontal Rainbow Gradient Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeightDp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(spectrumColors)),
            )

            // Circular Color Picker Dot Thumb with White Outer Border
            Box(
                modifier = Modifier
                    .offset { IntOffset((thumbOffsetPx - thumbRadiusPx).toInt(), 0) }
                    .size(thumbRadiusDp * 2)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(2.5.dp, Color.White, CircleShape),
            )
        }
    }
}

private infix fun Int.shrink(shift: Int): Int = this ushr shift
