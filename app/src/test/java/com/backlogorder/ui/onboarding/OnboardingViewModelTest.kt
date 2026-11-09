package com.backlogorder.ui.onboarding

import com.backlogorder.domain.model.ThemeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validApiKeyPersistsKeyAndMarksOnboardingComplete() = runTest {
        val apiKeyProvider = InMemoryApiKeyProvider()
        val settingsRepository = InMemorySettingsRepository()
        val viewModel = OnboardingViewModel(
            apiKeyProvider = apiKeyProvider,
            settingsRepository = settingsRepository,
            validator = object : ApiKeyValidator {
                override suspend fun validate(apiKey: String): ApiKeyValidationStatus =
                    if (apiKey == "valid-key") ApiKeyValidationStatus.SUCCESS else ApiKeyValidationStatus.INVALID
            },
        )

        viewModel.onApiKeyChanged("valid-key")
        viewModel.validateKey()
        advanceUntilIdle()

        assertEquals(ApiKeyValidationStatus.SUCCESS, viewModel.uiState.value.validationState)
        assertEquals("valid-key", apiKeyProvider.key.first())
        assertTrue(settingsRepository.onboardingComplete.first())
    }

    @Test
    fun invalidUnauthorizedAndNetworkStatesAreReported() = runTest {
        val apiKeyProvider = InMemoryApiKeyProvider()
        val settingsRepository = InMemorySettingsRepository()
        val viewModel = OnboardingViewModel(apiKeyProvider, settingsRepository, SimpleApiKeyValidator())

        viewModel.onApiKeyChanged("invalid")
        viewModel.validateKey()
        advanceUntilIdle()
        assertEquals(ApiKeyValidationStatus.INVALID, viewModel.uiState.value.validationState)

        viewModel.onApiKeyChanged("unauthorized")
        viewModel.validateKey()
        advanceUntilIdle()
        assertEquals(ApiKeyValidationStatus.UNAUTHORIZED, viewModel.uiState.value.validationState)

        viewModel.onApiKeyChanged("network")
        viewModel.validateKey()
        advanceUntilIdle()
        assertEquals(ApiKeyValidationStatus.NETWORK_ERROR, viewModel.uiState.value.validationState)
    }

    @Test
    fun continueWithoutKeyClearsStoredValueAndMarksComplete() = runTest {
        val apiKeyProvider = InMemoryApiKeyProvider("existing-key")
        val settingsRepository = InMemorySettingsRepository(initialCompleted = false, initialTheme = ThemeId.VIOLET)
        val viewModel = OnboardingViewModel(apiKeyProvider, settingsRepository)

        viewModel.continueWithoutKey()
        advanceUntilIdle()

        assertNull(apiKeyProvider.key.first())
        assertTrue(settingsRepository.onboardingComplete.first())
    }
}
