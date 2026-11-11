package com.backlogorder.data.repository

import androidx.room.Transaction
import androidx.room.withTransaction
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.ListGameEntity
import com.backlogorder.data.local.StringListConverter
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.repository.GamesRepository
import android.content.Context
import com.backlogorder.util.CoverImageDownloader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

open class RoomGamesRepository(
    private val database: BacklogOrderDatabase,
    private val context: Context? = null,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : GamesRepository {

    private val listConverter = StringListConverter()
    private val json = Json { ignoreUnknownKeys = true }
    private val coverDownloader: CoverImageDownloader? = context?.let { CoverImageDownloader(it) }

    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> =
        database.gamesDao().observeEntriesForList(listId).map { entries ->
            resolveEntries(entries)
        }

    override fun observeAllEntries(): Flow<List<ListEntry>> =
        database.gamesDao().observeAllEntries().map { entries ->
            resolveEntries(entries)
        }

    open suspend fun getGameById(gameId: String): GameEntity? = database.gamesDao().getGameById(gameId)

    open suspend fun updateCoverFields(gameId: String, localPath: String?, url: String?) {
        database.gamesDao().updateCoverFields(gameId, localPath, url)
    }

    @Transaction
    override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry {
        val existingGame = findMatchingGame(game)
        val gameId = if (existingGame != null) {
            val remoteUrl = existingGame.backgroundImageUrl
            if (remoteUrl != null && existingGame.backgroundImage == null && coverDownloader != null) {
                val downloadRes = coverDownloader.download(existingGame.id, remoteUrl)
                if (downloadRes.isSuccess) {
                    val localPath = downloadRes.getOrNull()
                    database.gamesDao().updateCoverFields(existingGame.id, localPath, remoteUrl)
                }
            }
            existingGame.id
        } else {
            createGame(game)
        }

        val existingEntry = database.gamesDao().findListGame(listId, gameId)
        if (existingEntry != null) {
            database.gamesDao().updateStatus(listId, gameId, status.name)
            return database.gamesDao().findListGame(listId, gameId)?.let { entry ->
                entry.toDomain(fetchGame(gameId))
            } ?: error("Entry for list $listId and game $gameId not found")
        }

        val lists = database.listDao().observeLists().first()
        val listName = lists.find { it.id == listId }?.name ?: ""
        val entity = ListGameEntity(
            id = UUID.randomUUID().toString(),
            listId = listId,
            gameId = gameId,
            status = status.name,
            addedAt = clock(),
            listName = listName,
        )
        database.gamesDao().insertListGame(entity)
        return entity.toDomain(fetchGame(gameId))
    }

    override suspend fun removeGameFromList(listId: String, gameId: String) {
        database.gamesDao().removeGameFromList(listId, gameId)
        database.gamesDao().deleteOrphanGames()
    }

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) {
        database.gamesDao().updateStatus(listId, gameId, status.name)
    }

    override suspend fun updateGame(game: Game) {
        val remoteUrl = game.backgroundImageUrl ?: if (game.backgroundImage?.startsWith("http", ignoreCase = true) == true) game.backgroundImage else null
        val initialLocalPath = if (game.backgroundImage?.startsWith("http", ignoreCase = true) == false) game.backgroundImage else null

        val localCoverPath = if (remoteUrl != null && coverDownloader != null) {
            val res = coverDownloader.download(game.id, remoteUrl)
            if (res.isSuccess) res.getOrNull() else initialLocalPath
        } else {
            initialLocalPath
        }

        database.gamesDao().upsertGame(
            GameEntity(
                id = game.id,
                rawgId = game.rawgId,
                name = game.name,
                backgroundImage = localCoverPath,
                backgroundImageUrl = remoteUrl,
                metacritic = game.metacritic,
                rating = game.rating,
                playtimeHours = game.playtimeHours,
                genres = listConverter.toString(game.genres),
                platforms = listConverter.toString(game.platforms),
                tags = listConverter.toString(game.tags),
                isManual = if (game.isManual) 1 else 0,
                franchiseName = game.franchiseName,
                franchiseOrder = game.franchiseOrder,
                createdAt = game.createdAt,
                updatedAt = game.updatedAt,
            ),
        )
    }

    override suspend fun setFranchise(gameId: String, name: String?, order: Int?) {
        database.gamesDao().setFranchise(gameId, name, order)
    }

    @Transaction
    suspend fun importData(payload: String) {
        database.withTransaction {
            val data = parsePayload(payload)
            val listIds = data.lists.map { it.id }.toSet()
            val gameIds = data.games.map { it.id }.toSet()
            require(data.listGames.all { it.listId in listIds }) { "Import payload contains a listGame pointing to an unknown list" }
            require(data.listGames.all { it.gameId in gameIds }) { "Import payload contains a listGame pointing to an unknown game" }

            for (list in data.lists) {
                database.listDao().insertList(
                    ListEntity(
                        id = list.id,
                        name = list.name,
                        createdAt = list.createdAt,
                        updatedAt = list.updatedAt,
                    ),
                )
            }
            for (game in data.games) {
                database.gamesDao().upsertGame(
                    GameEntity(
                        id = game.id,
                        rawgId = game.rawgId,
                        name = game.name,
                        backgroundImage = game.backgroundImage,
                        metacritic = game.metacritic,
                        rating = game.rating,
                        playtimeHours = game.playtimeHours,
                        genres = listConverter.toString(game.genres),
                        platforms = listConverter.toString(game.platforms),
                        tags = listConverter.toString(game.tags),
                        isManual = if (game.isManual) 1 else 0,
                        franchiseName = game.franchiseName,
                        franchiseOrder = game.franchiseOrder,
                        createdAt = game.createdAt,
                        updatedAt = game.updatedAt,
                    ),
                )
            }
            for (link in data.listGames) {
                val listName = data.lists.find { it.id == link.listId }?.name ?: ""
                database.gamesDao().insertListGame(
                    ListGameEntity(
                        id = link.id,
                        listId = link.listId,
                        gameId = link.gameId,
                        status = link.status.name,
                        addedAt = link.addedAt,
                        listName = listName,
                    ),
                )
            }
        }
    }

    private suspend fun findMatchingGame(game: NewGame): GameEntity? {
        if (game.rawgId != null) {
            return database.gamesDao().findByRawgId(game.rawgId)
        }
        return database.gamesDao().findManualByName(game.name)
    }

    private suspend fun createGame(game: NewGame): String {
        val id = UUID.randomUUID().toString()
        val createdAt = clock()

        val remoteUrl = game.backgroundImageUrl ?: if (game.backgroundImage?.startsWith("http", ignoreCase = true) == true) game.backgroundImage else null
        val initialLocalPath = if (game.backgroundImage?.startsWith("http", ignoreCase = true) == false) game.backgroundImage else null

        val localCoverPath = if (remoteUrl != null && coverDownloader != null) {
            val res = coverDownloader.download(id, remoteUrl)
            if (res.isSuccess) res.getOrNull() else initialLocalPath
        } else {
            initialLocalPath
        }

        val entity = GameEntity(
            id = id,
            rawgId = game.rawgId,
            name = game.name,
            backgroundImage = localCoverPath,
            backgroundImageUrl = remoteUrl,
            metacritic = game.metacritic,
            rating = game.rating,
            playtimeHours = game.playtimeHours,
            genres = listConverter.toString(game.genres),
            platforms = listConverter.toString(game.platforms),
            tags = listConverter.toString(game.tags),
            isManual = if (game.isManual) 1 else 0,
            franchiseName = game.franchiseName,
            franchiseOrder = game.franchiseOrder,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
        database.gamesDao().insertGame(entity)
        return id
    }

    private suspend fun fetchGame(gameId: String): Game =
        database.gamesDao().getGameById(gameId)?.toDomain() ?: error("Game $gameId not found")

    private suspend fun resolveEntries(entries: List<ListGameEntity>): List<ListEntry> {
        if (entries.isEmpty()) return emptyList()
        val ids = entries.map { it.gameId }.distinct()
        val games = database.gamesDao().getGamesByIds(ids).associateBy { it.id }
        return entries.mapNotNull { entry ->
            val gameEntity = games[entry.gameId] ?: return@mapNotNull null
            entry.toDomain(gameEntity.toDomain())
        }
    }

    @Serializable
    private data class ImportPayload(
        val version: Int = 1,
        val lists: List<ImportList> = emptyList(),
        val games: List<ImportGame> = emptyList(),
        @SerialName("listGames") val listGames: List<ImportLink> = emptyList(),
    )

    @Serializable
    private data class ImportList(
        val id: String,
        val name: String,
        @SerialName("createdAt") val createdAt: Long,
        @SerialName("updatedAt") val updatedAt: Long,
    )

    @Serializable
    private data class ImportGame(
        val id: String,
        @SerialName("rawgId") val rawgId: Int? = null,
        val name: String,
        @SerialName("backgroundImage") val backgroundImage: String? = null,
        val metacritic: Int? = null,
        val rating: Double? = null,
        @SerialName("playtimeHours") val playtimeHours: Double? = null,
        val genres: List<String> = emptyList(),
        val platforms: List<String> = emptyList(),
        val tags: List<String> = emptyList(),
        @SerialName("isManual") val isManual: Boolean = false,
        @SerialName("franchiseName") val franchiseName: String? = null,
        @SerialName("franchiseOrder") val franchiseOrder: Int? = null,
        @SerialName("createdAt") val createdAt: Long = 0L,
        @SerialName("updatedAt") val updatedAt: Long = 0L,
    )

    @Serializable
    private data class ImportLink(
        val id: String,
        @SerialName("listId") val listId: String,
        @SerialName("gameId") val gameId: String,
        val status: GameStatus,
        @SerialName("addedAt") val addedAt: Long,
    )

    private fun parsePayload(payload: String): ImportPayload {
        require(payload.isNotBlank()) { "Import payload is empty" }
        val decoded = json.decodeFromString(ImportPayload.serializer(), payload)
        require(decoded.version == 1) { "Unsupported export version: ${decoded.version}" }
        return decoded
    }
}
