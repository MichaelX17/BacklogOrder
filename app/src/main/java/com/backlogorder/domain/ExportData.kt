package com.backlogorder.domain

import com.backlogorder.domain.model.GameStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.security.MessageDigest
import java.util.UUID

object UuidSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

sealed class ExportError : Exception() {
    data class StorageFull(val neededBytes: Long? = null) : ExportError()
    data class WriteFailed(override val cause: Throwable) : ExportError()
    data class VerificationFailed(val reason: String) : ExportError()
    data class ShareFailed(override val cause: Throwable) : ExportError()
}

/**
 * Root export data model matching docs/export-schema-v1.md
 */
@Serializable
data class ExportData(
    val version: Int = 1,
    val app: String = "BacklogOrder",
    val appVersion: String = "unknown",
    val format: String = "json",
    val contentHashAlgorithm: String = "sha256-v1",
    val exportedAt: Long = System.currentTimeMillis(),
    val lists: List<ExportedList> = emptyList(),
    val games: List<ExportedGame> = emptyList(),
    val listGames: List<ExportedListGame> = emptyList()
) {
    init {
        require(version == 1) { "Unsupported export version: $version. Expected 1." }
        require(app == "BacklogOrder") { "Invalid app name: $app. Expected 'BacklogOrder'." }
    }
}

@Serializable
data class ExportedList(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    @SerialName("name") val listName: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    init {
        require(listName.isNotBlank()) { "List name cannot be blank." }
        require(createdAt > 0) { "List createdAt must be positive." }
        require(updatedAt > 0) { "List updatedAt must be positive." }
    }
}

@Serializable
data class ExportedGame(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    val rawgId: Int? = null,
    @SerialName("name") val gameName: String,
    val backgroundImage: String? = null,
    val backgroundImageUrl: String? = null,
    val metacritic: Int? = null,
    val rating: Double? = null,
    val playtimeHours: Double? = null,
    val genres: List<String>,
    val platforms: List<String>,
    val tags: List<String>,
    val isManual: Boolean,
    val franchiseName: String? = null,
    val franchiseOrder: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val contentHash: String = ""
) {
    init {
        require(gameName.isNotBlank()) { "Game name cannot be blank." }
        require(createdAt > 0) { "Game createdAt must be positive." }
        require(updatedAt > 0) { "Game updatedAt must be positive." }
        if (metacritic != null) {
            require(metacritic in 0..100) { "Metacritic must be 0-100." }
        }
        if (rating != null) {
            require(rating >= 0.0 && rating <= 5.0) { "Rating must be 0.0-5.0." }
        }
        if (playtimeHours != null) {
            require(playtimeHours >= 0.0) { "Playtime hours must be ≥ 0." }
        }
    }
}

@Serializable
data class ExportedListGame(
    @Serializable(with = UuidSerializer::class)
    val id: UUID,
    @Serializable(with = UuidSerializer::class)
    val listId: UUID,
    @Serializable(with = UuidSerializer::class)
    val gameId: UUID,
    @SerialName("status") val status: GameStatus,
    val addedAt: Long
) {
    init {
        require(listId != gameId) { "List ID and Game ID must be different." }
        require(addedAt > 0) { "AddedAt must be positive." }
        require(status in setOf(GameStatus.BACKLOG, GameStatus.PLAYING, GameStatus.COMPLETED, GameStatus.DROPPED)) {
            "Status must be one of: BACKLOG, PLAYING, COMPLETED, DROPPED"
        }
    }
}

object GameContentHasher {
    fun computeHash(
        name: String,
        rawgId: Int?,
        metacritic: Int?,
        rating: Double?,
        playtimeHours: Double?,
        genres: List<String>,
        platforms: List<String>,
        tags: List<String>,
        isManual: Boolean,
        franchiseName: String?,
        franchiseOrder: Int?,
    ): String {
        val sortedGenres = genres.sorted().joinToString(",")
        val sortedPlatforms = platforms.sorted().joinToString(",")
        val sortedTags = tags.sorted().joinToString(",")

        val canonicalString = buildString {
            append(name).append("|")
            append(rawgId?.toString() ?: "null").append("|")
            append(metacritic?.toString() ?: "null").append("|")
            append(rating?.toString() ?: "null").append("|")
            append(playtimeHours?.toString() ?: "null").append("|")
            append(sortedGenres).append("|")
            append(sortedPlatforms).append("|")
            append(sortedTags).append("|")
            append(isManual).append("|")
            append(franchiseName ?: "null").append("|")
            append(franchiseOrder?.toString() ?: "null")
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(canonicalString.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun computeHash(game: ExportedGame): String {
        return computeHash(
            name = game.gameName,
            rawgId = game.rawgId,
            metacritic = game.metacritic,
            rating = game.rating,
            playtimeHours = game.playtimeHours,
            genres = game.genres,
            platforms = game.platforms,
            tags = game.tags,
            isManual = game.isManual,
            franchiseName = game.franchiseName,
            franchiseOrder = game.franchiseOrder,
        )
    }
}

/**
 * Validation results for export/import operations
 */
sealed class ValidationResult {
    object Valid : ValidationResult()

    data class Invalid(val errors: List<String>) : ValidationResult()

    companion object {
        fun validate(data: ExportData): ValidationResult {
            val errors = mutableListOf<String>()

            if (data.version != 1) {
                errors.add("Invalid version: ${data.version}. Expected 1.")
            }
            if (data.app != "BacklogOrder") {
                errors.add("Invalid app name: ${data.app}. Expected 'BacklogOrder'.")
            }
            if (data.lists.isEmpty()) {
                errors.add("Export must contain at least one list.")
            }

            data.lists.forEachIndexed { index, list ->
                if (list.listName.isBlank()) {
                    errors.add("List at index $index has blank name.")
                }
                if (list.createdAt <= 0) {
                    errors.add("List '${list.listName}' has invalid createdAt: ${list.createdAt}.")
                }
                if (list.updatedAt <= 0) {
                    errors.add("List '${list.listName}' has invalid updatedAt: ${list.updatedAt}.")
                }
            }

            data.games.forEachIndexed { index, game ->
                if (game.gameName.isBlank()) {
                    errors.add("Game at index $index has blank name.")
                }
                if (game.createdAt <= 0) {
                    errors.add("Game '${game.gameName}' has invalid createdAt: ${game.createdAt}.")
                }
                if (game.updatedAt <= 0) {
                    errors.add("Game '${game.gameName}' has invalid updatedAt: ${game.updatedAt}.")
                }
                if (game.metacritic != null && game.metacritic !in 0..100) {
                    errors.add("Game '${game.gameName}' has invalid metacritic: ${game.metacritic}. Must be 0-100.")
                }
                if (game.rating != null && (game.rating < 0.0 || game.rating > 5.0)) {
                    errors.add("Game '${game.gameName}' has invalid rating: ${game.rating}. Must be 0.0-5.0.")
                }
                if (game.playtimeHours != null && game.playtimeHours < 0.0) {
                    errors.add("Game '${game.gameName}' has invalid playtimeHours: ${game.playtimeHours}. Must be ≥ 0.")
                }
            }

            data.listGames.forEachIndexed { index, listGame ->
                if (listGame.listId == listGame.gameId) {
                    errors.add("ListGame at index $index has same listId and gameId.")
                }
                if (listGame.addedAt <= 0) {
                    errors.add("ListGame referencing list ${listGame.listId} has invalid addedAt: ${listGame.addedAt}.")
                }
                if (listGame.status !in setOf(GameStatus.BACKLOG, GameStatus.PLAYING, GameStatus.COMPLETED, GameStatus.DROPPED)) {
                    errors.add("ListGame at index $index has invalid status: ${listGame.status}.")
                }
            }

            val listIds = data.lists.map { it.id }
            val gameIds = data.games.map { it.id }

            data.listGames.forEach { listGame ->
                if (listGame.listId !in listIds) {
                    errors.add("ListGame references non-existent list: ${listGame.listId}")
                }
                if (listGame.gameId !in gameIds) {
                    errors.add("ListGame references non-existent game: ${listGame.gameId}")
                }
            }

            return if (errors.isEmpty()) Valid else Invalid(errors)
        }

        fun validateStructure(data: ExportData): ValidationResult = validate(data)

        fun validateForRestore(data: ExportData): ValidationResult {
            val structureResult = validateStructure(data)
            if (structureResult is Invalid) {
                return structureResult
            }

            val duplicatePairs = data.listGames
                .groupBy { Pair(it.listId, it.gameId) }
                .filter { it.value.size > 1 }

            if (duplicatePairs.isNotEmpty()) {
                val errors = duplicatePairs.map { (pair, entries) ->
                    "Duplicate listGame entries for list ${pair.first} and game ${pair.second}: ${entries.size} entries."
                }
                return Invalid(errors)
            }

            return Valid
        }
    }
}
