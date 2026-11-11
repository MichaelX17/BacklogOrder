package com.backlogorder.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.room.withTransaction
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.ListGameEntity
import com.backlogorder.data.local.StringListConverter
import com.backlogorder.di.AppDispatchers
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.ExportError
import com.backlogorder.domain.ExportedGame
import com.backlogorder.domain.ExportedList
import com.backlogorder.domain.ExportedListGame
import com.backlogorder.domain.GameContentHasher
import com.backlogorder.domain.ValidationResult
import com.backlogorder.domain.error.AppError.Validation
import com.backlogorder.domain.import.ConflictResolution
import com.backlogorder.domain.import.FieldMergeSelection
import com.backlogorder.domain.import.ImportConflict
import com.backlogorder.domain.import.ImportMode
import com.backlogorder.domain.import.ImportPlan
import com.backlogorder.domain.import.ImportReport
import com.backlogorder.domain.import.LocalSnapshot
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
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.UUID

class RoomSettingsRepository(
    private val context: Context,
    private val database: BacklogOrderDatabase,
    private val apiKeyProvider: ApiKeyProvider,
    private val dispatchers: AppDispatchers = AppDispatchers(),
) : SettingsRepository {

    init {
        cleanupStaleExportTemps()
    }

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

    private fun parseUuid(id: String): UUID = try {
        UUID.fromString(id)
    } catch (e: Exception) {
        UUID.nameUUIDFromBytes(id.toByteArray())
    }

    fun exportLocalPath(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val file = runCatching {
            if (raw.startsWith("file://", ignoreCase = true)) {
                File(Uri.parse(raw).path ?: return null)
            } else {
                File(raw)
            }
        }.getOrNull() ?: return null

        return if (file.exists() && file.length() > 0) raw else null
    }

    fun cleanupStaleExportTemps() {
        try {
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return
            val oneHourAgo = System.currentTimeMillis() - 3_600_000L
            val tempFiles = docsDir.listFiles { _, name ->
                name.startsWith("BacklogOrder_Export_") && name.endsWith(".json.tmp")
            } ?: return

            for (tempFile in tempFiles) {
                if (tempFile.lastModified() < oneHourAgo) {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun exportData(): Result<ExportData> = try {
        database.gamesDao().deleteOrphanGames()
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
                id = parseUuid(list.id),
                listName = list.name,
                createdAt = list.createdAt,
                updatedAt = list.updatedAt,
            )
        }

        var missingCovers = 0
        val exportedGames = games.map { game ->
            val localCoverPath = exportLocalPath(game.backgroundImage)
            if (game.backgroundImage != null && localCoverPath == null) {
                missingCovers++
            }
            val genres = StringListConverter().fromString(game.genres)
            val platforms = StringListConverter().fromString(game.platforms)
            val tags = StringListConverter().fromString(game.tags)
            val isManual = (game.isManual == 1)

            val contentHash = GameContentHasher.computeHash(
                name = game.name,
                rawgId = game.rawgId,
                metacritic = game.metacritic,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
                genres = genres,
                platforms = platforms,
                tags = tags,
                isManual = isManual,
                franchiseName = game.franchiseName,
                franchiseOrder = game.franchiseOrder,
            )

            ExportedGame(
                id = parseUuid(game.id),
                rawgId = game.rawgId,
                gameName = game.name,
                backgroundImage = localCoverPath,
                backgroundImageUrl = game.backgroundImageUrl,
                metacritic = game.metacritic,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
                genres = genres,
                platforms = platforms,
                tags = tags,
                isManual = isManual,
                franchiseName = game.franchiseName,
                franchiseOrder = game.franchiseOrder,
                createdAt = game.createdAt,
                updatedAt = game.updatedAt,
                contentHash = contentHash,
            )
        }

        if (missingCovers > 0) {
            android.util.Log.i("RoomSettingsRepository", "Exported ${exportedGames.size} games ($missingCovers cover image files were unavailable/missing)")
        }

        val exportedListGames = listGamesWithDetails.map { (list, game, listGame) ->
            ExportedListGame(
                id = parseUuid(listGame.id),
                listId = parseUuid(list.id),
                gameId = parseUuid(game.id),
                status = GameStatus.valueOf(listGame.status),
                addedAt = listGame.addedAt,
            )
        }

        val appVersion = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

        val exportData = ExportData(
            appVersion = appVersion,
            lists = exportedLists,
            games = exportedGames,
            listGames = exportedListGames,
        )

        Result.success(exportData)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun shareExportedData(exportedData: ExportData): Result<Uri> = withContext(dispatchers.io) {
        try {
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: return@withContext Result.failure(ExportError.StorageFull())

            val json = Json { ignoreUnknownKeys = true; coerceInputValues = false }
            val jsonString = json.encodeToString(exportedData)

            val baseName = "BacklogOrder_Export_${System.currentTimeMillis()}"
            val tmpFile = File(docsDir, "$baseName.json.tmp")
            val finalFile = File(docsDir, "$baseName.json")

            try {
                FileOutputStream(tmpFile).use { fos ->
                    OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                        writer.write(jsonString)
                        writer.flush()
                    }
                }
            } catch (e: Exception) {
                tmpFile.delete()
                return@withContext Result.failure(ExportError.WriteFailed(e))
            }

            val verificationError = verifyExportFile(tmpFile, exportedData, json)
            if (verificationError != null) {
                tmpFile.delete()
                return@withContext Result.failure(ExportError.VerificationFailed(verificationError))
            }

            val renamed = tmpFile.renameTo(finalFile)
            if (!renamed) {
                try {
                    tmpFile.copyTo(finalFile, overwrite = true)
                    tmpFile.delete()
                    val secondVerification = verifyExportFile(finalFile, exportedData, json)
                    if (secondVerification != null) {
                        finalFile.delete()
                        return@withContext Result.failure(ExportError.VerificationFailed("Destination verification failed: $secondVerification"))
                    }
                } catch (e: Exception) {
                    tmpFile.delete()
                    finalFile.delete()
                    return@withContext Result.failure(ExportError.WriteFailed(e))
                }
            }

            val contentUri = try {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    finalFile,
                )
            } catch (e: Exception) {
                return@withContext Result.failure(ExportError.ShareFailed(e))
            }

            Result.success(contentUri)
        } catch (e: Exception) {
            Result.failure(ExportError.WriteFailed(e))
        }
    }

    override suspend fun downloadExportedData(exportedData: ExportData): Result<String> = withContext(dispatchers.io) {
        try {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = false }
            val jsonString = json.encodeToString(exportedData)
            val fileName = "BacklogOrder_Export_${System.currentTimeMillis()}.json"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(ExportError.WriteFailed(IllegalStateException("Failed to create MediaStore entry in Downloads")))

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    OutputStreamWriter(os, Charsets.UTF_8).use { writer ->
                        writer.write(jsonString)
                        writer.flush()
                    }
                } ?: return@withContext Result.failure(ExportError.WriteFailed(IllegalStateException("Failed to open output stream")))

                Result.success("BacklogOrder_Export.json")
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                        writer.write(jsonString)
                        writer.flush()
                    }
                }
                Result.success(targetFile.name)
            }
        } catch (e: Exception) {
            Result.failure(ExportError.WriteFailed(e))
        }
    }

    private fun verifyExportFile(
        file: File,
        expectedData: ExportData,
        json: Json,
    ): String? {
        if (!file.exists() || file.length() == 0L) {
            return "File does not exist or is empty"
        }

        val parsed = try {
            json.decodeFromString<ExportData>(file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            return "Failed to parse generated JSON: ${e.message}"
        }

        if (parsed.lists.size != expectedData.lists.size) {
            return "List count mismatch: expected ${expectedData.lists.size}, got ${parsed.lists.size}"
        }
        if (parsed.games.size != expectedData.games.size) {
            return "Game count mismatch: expected ${expectedData.games.size}, got ${parsed.games.size}"
        }
        if (parsed.listGames.size != expectedData.listGames.size) {
            return "ListGame count mismatch: expected ${expectedData.listGames.size}, got ${parsed.listGames.size}"
        }

        val listIds = parsed.lists.map { it.id }.toSet()
        val gameIds = parsed.games.map { it.id }.toSet()

        for (link in parsed.listGames) {
            if (link.listId !in listIds) {
                return "Referenced listId ${link.listId} not found in lists"
            }
            if (link.gameId !in gameIds) {
                return "Referenced gameId ${link.gameId} not found in games"
            }
        }

        for (game in parsed.games) {
            val path = game.backgroundImage
            if (!path.isNullOrBlank()) {
                val localFile = runCatching {
                    if (path.startsWith("file://", ignoreCase = true)) {
                        File(Uri.parse(path).path ?: path)
                    } else {
                        File(path)
                    }
                }.getOrNull()
                if (localFile == null || !localFile.exists() || localFile.length() == 0L) {
                    return "Exported cover file for game '${game.gameName}' does not exist on disk"
                }
            }
        }

        return null
    }

    suspend fun savePreImportSnapshot(): Result<String> = withContext(dispatchers.io) {
        try {
            val exportRes = exportData()
            val exportData = exportRes.getOrElse { return@withContext Result.failure(it) }
            val cacheDir = context.cacheDir ?: return@withContext Result.failure(IllegalStateException("Cache directory unavailable"))
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = false }
            val jsonString = json.encodeToString(exportData)
            val fileName = "BacklogOrder_PreImport_${System.currentTimeMillis()}.json"
            val snapshotFile = File(cacheDir, fileName)
            FileOutputStream(snapshotFile).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            }
            if (snapshotFile.exists() && snapshotFile.length() > 0) {
                Result.success(snapshotFile.absolutePath)
            } else {
                Result.failure(IllegalStateException("Pre-import snapshot file creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLocalSnapshot(): LocalSnapshot = withContext(dispatchers.io) {
        val lists = database.listDao().getAllListsDirect().associateBy { it.id }
        val games = database.gamesDao().getAllGames().associateBy { it.id }
        val gamesByRawgId = games.values.filter { it.rawgId != null }.associateBy { it.rawgId!! }
        val allLists = database.listDao().getAllListsDirect()
        val listGames = mutableListOf<ListGameEntity>()
        for (l in allLists) {
            listGames.addAll(database.gamesDao().listGamesFor(l.id))
        }
        val listGamesMap = listGames.associateBy { it.id }
        val listGamesByPair = listGames.associateBy { Pair(it.listId, it.gameId) }

        LocalSnapshot(
            lists = lists,
            games = games,
            gamesByRawgId = gamesByRawgId,
            listGames = listGamesMap,
            listGamesByPair = listGamesByPair,
        )
    }

    suspend fun applyMergePlan(
        plan: ImportPlan,
        resolutions: Map<String, ConflictResolution>,
        fieldSelections: Map<String, FieldMergeSelection> = emptyMap(),
    ): Result<ImportReport> = withContext(dispatchers.io) {
        try {
            val snapshotPath = savePreImportSnapshot().getOrElse {
                return@withContext Result.failure(it)
            }

            val toUpdateLists = plan.toUpdateLists.toMutableList()
            val toUpdateGames = plan.toUpdateGames.toMutableList()
            val toUpdateListGames = plan.toUpdateListGames.toMutableList()
            val stringConverter = StringListConverter()

            for (conflict in plan.conflicts) {
                val res = resolutions[conflict.backupItemId] ?: ConflictResolution.KEEP_LOCAL
                when (conflict) {
                    is ImportConflict.GameMetadata -> {
                        when (res) {
                            ConflictResolution.KEEP_LOCAL -> { /* Keep local */ }
                            ConflictResolution.KEEP_BACKUP -> {
                                val b = conflict.backup
                                toUpdateGames += conflict.local.copy(
                                    name = b.gameName,
                                    backgroundImage = exportLocalPath(b.backgroundImage),
                                    backgroundImageUrl = b.backgroundImageUrl,
                                    metacritic = b.metacritic,
                                    rating = b.rating,
                                    playtimeHours = b.playtimeHours,
                                    genres = stringConverter.toString(b.genres),
                                    platforms = stringConverter.toString(b.platforms),
                                    tags = stringConverter.toString(b.tags),
                                    isManual = if (b.isManual) 1 else 0,
                                    franchiseName = b.franchiseName,
                                    franchiseOrder = b.franchiseOrder,
                                    createdAt = b.createdAt,
                                    updatedAt = b.updatedAt,
                                )
                            }
                            ConflictResolution.MERGE_FIELDS -> {
                                val sel = fieldSelections[conflict.backupItemId] ?: FieldMergeSelection()
                                val l = conflict.local
                                val b = conflict.backup
                                val localGenres = stringConverter.fromString(l.genres)
                                val localPlatforms = stringConverter.fromString(l.platforms)
                                val localTags = stringConverter.fromString(l.tags)

                                val mergedGenres = if (sel.useUnionGenres) (localGenres + b.genres).distinct() else localGenres
                                val mergedPlatforms = if (sel.useUnionPlatforms) (localPlatforms + b.platforms).distinct() else localPlatforms
                                val mergedTags = if (sel.useUnionTags) (localTags + b.tags).distinct() else localTags
                                val maxPlaytime = if (sel.useMaxPlaytimeHours) maxOf(l.playtimeHours ?: 0.0, b.playtimeHours ?: 0.0) else l.playtimeHours

                                toUpdateGames += l.copy(
                                    name = if (sel.useLocalName) l.name else b.gameName,
                                    metacritic = if (sel.useLocalMetacritic) l.metacritic else b.metacritic,
                                    rating = if (sel.useLocalRating) l.rating else b.rating,
                                    playtimeHours = maxPlaytime,
                                    genres = stringConverter.toString(mergedGenres),
                                    platforms = stringConverter.toString(mergedPlatforms),
                                    tags = stringConverter.toString(mergedTags),
                                    isManual = if (sel.useOrIsManual) (if (l.isManual == 1 || b.isManual) 1 else 0) else l.isManual,
                                    franchiseName = if (sel.useLocalFranchise) (l.franchiseName ?: b.franchiseName) else b.franchiseName,
                                    franchiseOrder = if (sel.useLocalFranchise) (l.franchiseOrder ?: b.franchiseOrder) else b.franchiseOrder,
                                    createdAt = if (sel.useMinCreatedAt) minOf(l.createdAt, b.createdAt) else l.createdAt,
                                    updatedAt = if (sel.useMaxUpdatedAt) maxOf(l.updatedAt, b.updatedAt) else l.updatedAt,
                                    backgroundImage = if (sel.useLocalCoverImage) (l.backgroundImage ?: exportLocalPath(b.backgroundImage)) else exportLocalPath(b.backgroundImage),
                                    backgroundImageUrl = if (sel.useLocalCoverUrl) (l.backgroundImageUrl ?: b.backgroundImageUrl) else b.backgroundImageUrl,
                                )
                            }
                            else -> {}
                        }
                    }
                    is ImportConflict.GameRawgId -> {
                        when (res) {
                            ConflictResolution.USE_LOCAL -> { /* Local game kept */ }
                            ConflictResolution.USE_BACKUP -> {
                                val b = conflict.backup
                                toUpdateGames += conflict.local.copy(
                                    name = b.gameName,
                                    backgroundImage = exportLocalPath(b.backgroundImage),
                                    backgroundImageUrl = b.backgroundImageUrl,
                                    metacritic = b.metacritic,
                                    rating = b.rating,
                                    playtimeHours = b.playtimeHours,
                                    genres = stringConverter.toString(b.genres),
                                    platforms = stringConverter.toString(b.platforms),
                                    tags = stringConverter.toString(b.tags),
                                    franchiseName = b.franchiseName,
                                    franchiseOrder = b.franchiseOrder,
                                    updatedAt = maxOf(conflict.local.updatedAt, b.updatedAt),
                                )
                            }
                            else -> {}
                        }
                    }
                    is ImportConflict.ListName -> {
                        if (res == ConflictResolution.KEEP_BACKUP) {
                            toUpdateLists += conflict.local.copy(
                                name = conflict.backup.listName,
                                updatedAt = conflict.backup.updatedAt,
                            )
                        }
                    }
                    is ImportConflict.Membership -> {
                        if (res == ConflictResolution.KEEP_BACKUP) {
                            toUpdateListGames += conflict.local.copy(
                                status = conflict.backup.status.name,
                                addedAt = conflict.backup.addedAt,
                            )
                        }
                    }
                }
            }

            database.withTransaction {
                for (l in plan.toAddLists) database.listDao().insertList(l)
                for (g in plan.toAddGames) database.gamesDao().insertGame(g)
                for (l in toUpdateLists) database.listDao().updateList(l)
                for (g in toUpdateGames) database.gamesDao().upsertGame(g)

                val currentLists = database.listDao().getAllListsDirect().associateBy { it.id }
                for (lg in plan.toAddListGames) {
                    val correctName = currentLists[lg.listId]?.name ?: lg.listName
                    database.gamesDao().insertListGame(lg.copy(listName = correctName))
                }
                for (lg in toUpdateListGames) {
                    val correctName = currentLists[lg.listId]?.name ?: lg.listName
                    database.gamesDao().insertListGame(lg.copy(listName = correctName))
                }
            }

            val report = ImportReport(
                mode = ImportMode.MERGE,
                addedLists = plan.toAddLists.size,
                addedGames = plan.toAddGames.size,
                addedMemberships = plan.toAddListGames.size,
                updatedLists = toUpdateLists.size,
                updatedGames = toUpdateGames.size,
                updatedMemberships = toUpdateListGames.size,
                skippedLists = plan.skippedLists,
                skippedGames = plan.skippedGames,
                skippedMemberships = plan.skippedMemberships,
                missingCoversSkipped = plan.missingCoversSkipped,
                snapshotPath = snapshotPath,
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyRestorePlan(backupData: ExportData): Result<ImportReport> = withContext(dispatchers.io) {
        try {
            val snapshotPath = savePreImportSnapshot().getOrElse {
                return@withContext Result.failure(it)
            }

            val validation = ValidationResult.validateForRestore(backupData)
            if (validation is ValidationResult.Invalid) {
                return@withContext Result.failure(Validation(validation.errors.joinToString("\n")))
            }

            database.withTransaction {
                database.gamesDao().deleteAllListGames()
                database.gamesDao().deleteAllGames()
                database.listDao().deleteAllLists()

                backupData.lists.forEach { list ->
                    database.listDao().insertList(
                        ListEntity(
                            id = list.id.toString(),
                            name = list.listName,
                            createdAt = list.createdAt,
                            updatedAt = list.updatedAt,
                        ),
                    )
                }

                backupData.games.forEach { game ->
                    database.gamesDao().insertGame(
                        GameEntity(
                            id = game.id.toString(),
                            rawgId = game.rawgId,
                            name = game.gameName,
                            backgroundImage = exportLocalPath(game.backgroundImage),
                            backgroundImageUrl = game.backgroundImageUrl,
                            metacritic = game.metacritic,
                            rating = game.rating,
                            playtimeHours = game.playtimeHours,
                            genres = StringListConverter().toString(game.genres),
                            platforms = StringListConverter().toString(game.platforms),
                            tags = StringListConverter().toString(game.tags),
                            isManual = if (game.isManual) 1 else 0,
                            franchiseName = game.franchiseName,
                            franchiseOrder = game.franchiseOrder,
                            createdAt = game.createdAt,
                            updatedAt = game.updatedAt,
                        ),
                    )
                }

                val currentLists = database.listDao().getAllListsDirect().associateBy { it.id }
                backupData.listGames.forEach { lg ->
                    val listName = currentLists[lg.listId.toString()]?.name ?: ""
                    database.gamesDao().insertListGame(
                        ListGameEntity(
                            id = lg.id.toString(),
                            listId = lg.listId.toString(),
                            gameId = lg.gameId.toString(),
                            status = lg.status.name,
                            addedAt = lg.addedAt,
                            listName = listName,
                        ),
                    )
                }
            }

            val report = ImportReport(
                mode = ImportMode.RESTORE,
                addedLists = backupData.lists.size,
                addedGames = backupData.games.size,
                addedMemberships = backupData.listGames.size,
                snapshotPath = snapshotPath,
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importData(jsonString: String): Result<Unit> {
        return try {
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = false }
            val importData = try {
                json.decodeFromString<ExportData>(jsonString)
            } catch (e: Exception) {
                return Result.failure(Validation(e.message ?: "Invalid import data"))
            }

            val restoreResult = applyRestorePlan(importData)
            if (restoreResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(restoreResult.exceptionOrNull() ?: Exception("Restore import failed"))
            }
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
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = false }

            val testData = ExportData(
                lists = listOf(
                    ExportedList(
                        id = UUID.randomUUID(),
                        listName = "Test List",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
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
                        updatedAt = System.currentTimeMillis(),
                        contentHash = "test_hash",
                    ),
                ),
                listGames = listOf(
                    ExportedListGame(
                        id = UUID.randomUUID(),
                        listId = UUID.randomUUID(),
                        gameId = UUID.randomUUID(),
                        status = GameStatus.BACKLOG,
                        addedAt = System.currentTimeMillis(),
                    ),
                ),
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
