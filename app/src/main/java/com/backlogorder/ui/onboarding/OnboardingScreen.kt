package com.backlogorder.ui.onboarding

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.data.remote.RawgRemoteDataSource
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.error.AppError
import com.backlogorder.domain.model.ThemeId
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.SettingsRepository
import com.backlogorder.ui.hud.BevelFrame
import com.backlogorder.ui.hud.PrimaryButton
import com.backlogorder.ui.theme.BacklogTheme
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ApiKeyValidationStatus {
    IDLE,
    CHECKING,
    INVALID,
    UNAUTHORIZED,
    NETWORK_ERROR,
    SUCCESS,
}

data class OnboardingUiState(
    val apiKey: String = "",
    val validationState: ApiKeyValidationStatus = ApiKeyValidationStatus.IDLE,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
)

interface ApiKeyValidator {
    suspend fun validate(apiKey: String): ApiKeyValidationStatus
}

class RawgApiKeyValidator(
    private val rawgRemoteDataSource: RawgRemoteDataSource,
) : ApiKeyValidator {
    override suspend fun validate(apiKey: String): ApiKeyValidationStatus {
        if (apiKey.isBlank()) return ApiKeyValidationStatus.INVALID
        return try {
            rawgRemoteDataSource.searchGames("mario", apiKey)
            ApiKeyValidationStatus.SUCCESS
        } catch (e: AppError.Unauthorized) {
            ApiKeyValidationStatus.UNAUTHORIZED
        } catch (e: AppError.Network) {
            ApiKeyValidationStatus.NETWORK_ERROR
        } catch (e: AppError.Timeout) {
            ApiKeyValidationStatus.NETWORK_ERROR
        } catch (e: Exception) {
            ApiKeyValidationStatus.INVALID
        }
    }
}

class SimpleApiKeyValidator : ApiKeyValidator {
    override suspend fun validate(apiKey: String): ApiKeyValidationStatus = when {
        apiKey.isBlank() -> ApiKeyValidationStatus.INVALID
        apiKey.equals("invalid", ignoreCase = true) -> ApiKeyValidationStatus.INVALID
        apiKey.equals("unauthorized", ignoreCase = true) -> ApiKeyValidationStatus.UNAUTHORIZED
        apiKey.equals("network", ignoreCase = true) -> ApiKeyValidationStatus.NETWORK_ERROR
        else -> ApiKeyValidationStatus.SUCCESS
    }
}

class InMemoryApiKeyProvider(initialKey: String? = null) : ApiKeyProvider {
    private val state = MutableStateFlow(initialKey)

    override val key: Flow<String?> = state

    override val currentKey: String? get() = state.value

    override suspend fun setKey(value: String) {
        state.value = value
    }

    override suspend fun clear() {
        state.value = null
    }
}

class InMemorySettingsRepository(
    initialCompleted: Boolean = false,
    initialTheme: ThemeId = ThemeId.VIOLET,
    initialDarkMode: Boolean = true,
    initialCustomColorHex: String = "7045F5",
) : SettingsRepository {
    override val onboardingComplete: MutableStateFlow<Boolean> = MutableStateFlow(initialCompleted)
    override val theme: MutableStateFlow<ThemeId> = MutableStateFlow(initialTheme)
    override val isDarkMode: MutableStateFlow<Boolean> = MutableStateFlow(initialDarkMode)
    override val customColorHex: MutableStateFlow<String> = MutableStateFlow(initialCustomColorHex)

    override suspend fun setOnboardingComplete(value: Boolean) {
        onboardingComplete.value = value
    }

    override suspend fun setTheme(themeId: ThemeId) {
        theme.value = themeId
    }

    override suspend fun setDarkMode(value: Boolean) {
        isDarkMode.value = value
    }

    override suspend fun setCustomColorHex(hex: String) {
        customColorHex.value = hex
    }

    override suspend fun exportData(): Result<ExportData> = Result.success(ExportData())
    override suspend fun shareExportedData(exportedData: ExportData): Result<Uri> = Result.success(Uri.EMPTY)
    override suspend fun importData(jsonString: String): Result<Unit> = Result.success(Unit)
    override suspend fun openDocumentForImport(): Result<String> = Result.success("")
    override fun validateExportSchema(): Result<Unit> = Result.success(Unit)
}

class OnboardingViewModel(
    private val apiKeyProvider: ApiKeyProvider = InMemoryApiKeyProvider(),
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository(),
    private val validator: ApiKeyValidator = SimpleApiKeyValidator(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    val onboardingComplete: StateFlow<Boolean> = settingsRepository.onboardingComplete
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun shouldShowOnboarding(): Boolean = !onboardingComplete.value

    fun onApiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            apiKey = value,
            validationState = ApiKeyValidationStatus.IDLE,
            errorMessage = null,
        )
    }

    fun validateKey() {
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) {
            _uiState.value = _uiState.value.copy(
                validationState = ApiKeyValidationStatus.INVALID,
                errorMessage = "API key is required.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                validationState = ApiKeyValidationStatus.CHECKING,
                isSubmitting = true,
                errorMessage = null,
            )

            val result = validator.validate(key)
            when (result) {
                ApiKeyValidationStatus.SUCCESS -> {
                    apiKeyProvider.setKey(key)
                    settingsRepository.setOnboardingComplete(true)
                    _uiState.value = _uiState.value.copy(
                        validationState = ApiKeyValidationStatus.SUCCESS,
                        isSubmitting = false,
                        errorMessage = null,
                    )
                }

                ApiKeyValidationStatus.INVALID -> {
                    _uiState.value = _uiState.value.copy(
                        validationState = ApiKeyValidationStatus.INVALID,
                        isSubmitting = false,
                        errorMessage = "The API key is invalid.",
                    )
                }

                ApiKeyValidationStatus.UNAUTHORIZED -> {
                    _uiState.value = _uiState.value.copy(
                        validationState = ApiKeyValidationStatus.UNAUTHORIZED,
                        isSubmitting = false,
                        errorMessage = "This API key is unauthorized or invalid.",
                    )
                }

                ApiKeyValidationStatus.NETWORK_ERROR -> {
                    _uiState.value = _uiState.value.copy(
                        validationState = ApiKeyValidationStatus.NETWORK_ERROR,
                        isSubmitting = false,
                        errorMessage = "Network error while validating the key. Please check your connection.",
                    )
                }

                else -> Unit
            }
        }
    }

    fun continueWithoutKey() {
        viewModelScope.launch {
            apiKeyProvider.clear()
            settingsRepository.setOnboardingComplete(true)
            _uiState.value = _uiState.value.copy(
                validationState = ApiKeyValidationStatus.SUCCESS,
                errorMessage = null,
                isSubmitting = false,
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit = {},
) {
    val context = LocalContext.current.applicationContext
    val container = (context as? BacklogOrderApplication)?.container
    val viewModel: OnboardingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (container != null) {
                    @Suppress("UNCHECKED_CAST")
                    return OnboardingViewModel(
                        apiKeyProvider = container.apiKeyProvider,
                        settingsRepository = container.settingsRepository,
                        validator = RawgApiKeyValidator(container.rawgRemoteDataSource),
                    ) as T
                }
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel() as T
            }
        },
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.validationState) {
        if (uiState.validationState == ApiKeyValidationStatus.SUCCESS) {
            onNavigateToHome()
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                BevelFrame(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "BacklogOrder",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.text,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect your RAWG API key or continue without one to manage your local backlog.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = viewModel::onApiKeyChanged,
                            label = { Text("RAWG API Key", color = colors.textMuted) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accent,
                                unfocusedBorderColor = colors.textMuted.copy(alpha = 0.5f),
                                focusedTextColor = colors.text,
                                unfocusedTextColor = colors.text,
                            ),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PrimaryButton(
                            text = if (uiState.isSubmitting) "Validating..." else "Validate Key",
                            onClick = viewModel::validateKey,
                            enabled = !uiState.isSubmitting && uiState.apiKey.isNotBlank(),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = viewModel::continueWithoutKey,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Continue without key", color = colors.textMuted)
                        }

                        uiState.errorMessage?.let { message ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = message,
                                color = colors.danger,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        if (uiState.validationState == ApiKeyValidationStatus.SUCCESS) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Key validated successfully.",
                                color = colors.success,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
