package com.backlogorder.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.domain.error.AppError.Validation
import com.backlogorder.domain.repository.ApiKeyProvider
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomSettingsRepositoryTest {
    private lateinit var database: BacklogOrderDatabase
    private lateinit var repository: RoomSettingsRepository
    private lateinit var context: Context
    private lateinit var apiKeyProvider: ApiKeyProvider
    private lateinit var scope: TestScope

    @Before
    fun setup() {
        scope = TestScope()
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, BacklogOrderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        apiKeyProvider = object : ApiKeyProvider {
            override val key = flowOf(null as String?)
            override suspend fun setKey(value: String) {}
            override suspend fun clear() {}
        }
        repository = RoomSettingsRepository(context, database, apiKeyProvider)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `validateExportSchema passes for valid data`() = scope.runTest {
        val result = repository.validateExportSchema()
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `importData returns failure when validation fails`() = scope.runTest {
        val invalidJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [],
            "games": [],
            "listGames": []
        }
        """.trimIndent()
        
        val result = repository.importData(invalidJson)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Validation)
        assertTrue(result.exceptionOrNull()?.message?.contains("at least one list") == true)
    }

    @Test
    fun `ValidationResult detects missing required fields`() = scope.runTest {
        val invalidJson = """
        {
            "version": 1,
            "app": "BacklogOrder",
            "lists": [],
            "games": [],
            "listGames": []
        }
        """.trimIndent()

        val result = repository.importData(invalidJson)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Validation)
    }
}
