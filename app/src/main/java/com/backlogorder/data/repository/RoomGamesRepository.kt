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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

class RoomGamesRepository(
    private val database: BacklogOrderDatabase,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : GamesRepository {

    private val listConverter = StringListConverter()
    private val json = Json { ignoreUnknownKeys = true }

    override fun observeEntriesForList(listId: String): Flow<List<ListEntry>> =
        database.gamesDao().observeEntriesForList(listId).map { entries ->
            resolveEntries(entries)
        }

    override fun observeAllEntries(): Flow<List<ListEntry>> =
        database.gamesDao().observeAllEntries().map { entries ->
            resolveEntries(entries)
        }

    @Transaction
    override suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry {
        val existingGame = findMatchingGame(game)
        val gameId = existingGame?.id ?: createGame(game)
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
    }

    override suspend fun updateStatus(listId: String, gameId: String, status: GameStatus) {
        database.gamesDao().updateStatus(listId, gameId, status.name)
    }

    override suspend fun updateGame(game: Game) {
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
        val entity = GameEntity(
            id = id,
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
