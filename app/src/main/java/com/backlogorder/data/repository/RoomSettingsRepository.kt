package com.backlogorder.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.room.withTransaction
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.ListGameEntity
import com.backlogorder.data.local.StringListConverter
import com.backlogorder.di.AppDispatchers
import com.backlogorder.domain.error.AppError.Validation
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.ExportedGame
import com.backlogorder.domain.ExportedList
import com.backlogorder.domain.ExportedListGame
import com.backlogorder.domain.ValidationResult
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ThemeId
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import java.io.File
import java.io.FileOutputStream

class RoomSettingsRepository(
    private val context: Context,
    private val database: BacklogOrderDatabase,
    private val apiKeyProvider: ApiKeyProvider,
    private val dispatchers: AppDispatchers = AppDispatchers(),
) : SettingsRepository {

    override val onboardingComplete: Flow<Boolean> = database.settingsDao().observeOnboardingComplete()
        .map { it?.toBoolean() ?: false }
    
    override val theme: Flow<ThemeId> = database.settingsDao().observeTheme()
        .map { ThemeId.fromStorage(it) }

    override val isDarkMode: Flow<Boolean> = database.settingsDao().observeDarkMode()
        .map { it?.toBoolean() ?: true }

    override val customColorHex: Flow<String> = database.settingsDao().observeCustomColorHex()
        .map { it ?: "7045F5" }
    
    override suspend fun setOnboardingComplete(value: Boolean) {
        database.settingsDao().setOnboardingComplete(value)
    }
    
    override suspend fun setTheme(themeId: ThemeId) {
        database.settingsDao().setTheme(themeId.name)
    }

    override suspend fun setDarkMode(value: Boolean) {
        database.settingsDao().setDarkMode(value)
    }

    override suspend fun setCustomColorHex(hex: String) {
        database.settingsDao().setCustomColorHex(hex)
    }
    
    override suspend fun exportData(): Result<ExportData> = try {
        val lists = database.listDao().observeLists().first()
        val games = database.gamesDao().getAllGames()
        val listGamesWithDetails = mutableListOf<Triple<ListEntity, GameEntity, ListGameEntity>>()
        
        for (list in lists) {
            val gamesInList = database.gamesDao().listGamesFor(list.id)
            for (link in gamesInList) {
                val game = database.gamesDao().getGameById(link.gameId)
                if (game != null) {
                    listGamesWithDetails.add(Triple(list, game, link))
                }
            }
        }
        
        val exportedLists = lists.map { list ->
            ExportedList(
                id = UUID.fromString(list.id),
                listName = list.name,
                createdAt = list.createdAt,
                updatedAt = list.updatedAt
            )
        }
        
        val exportedGames = games.filter { it.isManual == 1 }.map { game ->
            ExportedGame(
                id = UUID.fromString(game.id),
                rawgId = game.rawgId,
                gameName = game.name,
                backgroundImage = game.backgroundImage,
                metacritic = game.metacritic,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
                genres = StringListConverter().fromString(game.genres),
                platforms = StringListConverter().fromString(game.platforms),
                tags = StringListConverter().fromString(game.tags),
                isManual = true,
                franchiseName = game.franchiseName,
                franchiseOrder = game.franchiseOrder,
                createdAt = game.createdAt,
                updatedAt = game.updatedAt
            )
        }
        
        val exportedListGames = listGamesWithDetails.map { (list, game, listGame) ->
            ExportedListGame(
                id = UUID.fromString(listGame.id),
                listId = UUID.fromString(list.id),
                gameId = UUID.fromString(game.id),
                status = GameStatus.valueOf(listGame.status),
                addedAt = listGame.addedAt
            )
        }
        
        val exportData = ExportData(
            lists = exportedLists,
            games = exportedGames,
            listGames = exportedListGames
        )
        
        Result.success(exportData)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun shareExportedData(exportedData: ExportData): Result<Uri> = withContext(dispatchers.io) {
        try {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val jsonString = json.encodeToString(exportedData)
            
            val fileName = "BacklogOrder_Export_${System.currentTimeMillis()}.json"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            
            Result.success(contentUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun importData(jsonString: String): Result<Unit> {
        return try {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            val importData = try {
                json.decodeFromString<ExportData>(jsonString)
            } catch (e: Exception) {
                return Result.failure(Validation(e.message ?: "Invalid import data"))
            }
            
            val validationResult = ValidationResult.validate(importData)
            when (validationResult) {
                is ValidationResult.Valid -> { /* Proceed */ }
                is ValidationResult.Invalid -> {
                    val errorMessage = "Import validation failed:\n${validationResult.errors.joinToString("\n")}"
                    return Result.failure(Validation(errorMessage))
                }
            }
            
            database.withTransaction {
                database.gamesDao().deleteAllListGames()
                database.gamesDao().deleteAllGames()
                database.listDao().deleteAllLists()

                importData.lists.forEach { exportedList ->
                    val listEntity = ListEntity(
                        id = exportedList.id.toString(),
                        name = exportedList.listName,
                        createdAt = exportedList.createdAt,
                        updatedAt = exportedList.updatedAt
                    )
                    database.listDao().insertList(listEntity)
                }

                importData.games.forEach { exportedGame ->
                    val gameEntity = GameEntity(
                        id = exportedGame.id.toString(),
                        rawgId = exportedGame.rawgId,
                        name = exportedGame.gameName,
                        backgroundImage = exportedGame.backgroundImage,
                        metacritic = exportedGame.metacritic,
                        rating = exportedGame.rating,
                        playtimeHours = exportedGame.playtimeHours,
                        genres = StringListConverter().toString(exportedGame.genres),
                        platforms = StringListConverter().toString(exportedGame.platforms),
                        tags = StringListConverter().toString(exportedGame.tags),
                        isManual = if (exportedGame.isManual) 1 else 0,
                        franchiseName = exportedGame.franchiseName,
                        franchiseOrder = exportedGame.franchiseOrder,
                        createdAt = exportedGame.createdAt,
                        updatedAt = exportedGame.updatedAt
                    )
                    database.gamesDao().insertGame(gameEntity)
                }

                val processedRelationships = mutableSetOf<Pair<String, String>>()
                importData.listGames.forEach { exportedListGame ->
                    val pair = Pair(exportedListGame.listId.toString(), exportedListGame.gameId.toString())
                    if (pair !in processedRelationships) {
                        val listGameEntity = ListGameEntity(
                            id = exportedListGame.id.toString(),
                            listId = pair.first,
                            gameId = pair.second,
                            status = exportedListGame.status.name,
                            addedAt = exportedListGame.addedAt,
                            listName = ""
                        )
                        database.gamesDao().insertListGame(listGameEntity)
                        processedRelationships.add(pair)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun openDocumentForImport(): Result<String> = try {
        Result.failure(Exception("Document picker not available in test environment"))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override fun validateExportSchema(): Result<Unit> {
        return try {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            
            val testData = ExportData(
                lists = listOf(
                    ExportedList(
                        id = UUID.randomUUID(),
                        listName = "Test List",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                ),
                games = listOf(
                    ExportedGame(
                        id = UUID.randomUUID(),
                        gameName = "Test Game",
                        isManual = true,
                        genres = listOf("Test"),
                        platforms = listOf("Test"),
                        tags = listOf("Test"),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                ),
                listGames = listOf(
                    ExportedListGame(
                        id = UUID.randomUUID(),
                        listId = UUID.randomUUID(),
                        gameId = UUID.randomUUID(),
                        status = GameStatus.BACKLOG,
                        addedAt = System.currentTimeMillis()
                    )
                )
            )
            
            val jsonString = json.encodeToString(testData)
            val parsedData = json.decodeFromString<ExportData>(jsonString)
            
            if (parsedData.lists.size != testData.lists.size) {
                return Result.failure(IllegalStateException("Schema validation failed: round-trip serialization produced different data"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
