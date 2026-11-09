package com.backlogorder.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry

@Entity(
    tableName = "lists",
)
data class ListEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
) {
    fun toDomain(): GameList = GameList(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

@Entity(
    tableName = "games",
    indices = [Index(value = ["rawg_id"], unique = true)],
)
data class GameEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "rawg_id") val rawgId: Int?,
    val name: String,
    @ColumnInfo(name = "background_image") val backgroundImage: String?,
    val metacritic: Int?,
    val rating: Double?,
    @ColumnInfo(name = "playtime_hours") val playtimeHours: Double?,
    val genres: String = "[]",
    val platforms: String = "[]",
    val tags: String = "[]",
    @ColumnInfo(name = "is_manual") val isManual: Int = 0,
    @ColumnInfo(name = "franchise_name") val franchiseName: String?,
    @ColumnInfo(name = "franchise_order") val franchiseOrder: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
) {
    fun toDomain(): Game = Game(
        id = id,
        rawgId = rawgId,
        name = name,
        backgroundImage = backgroundImage,
        metacritic = metacritic,
        rating = rating,
        playtimeHours = playtimeHours,
        genres = StringListConverter().fromString(genres),
        platforms = StringListConverter().fromString(platforms),
        tags = StringListConverter().fromString(tags),
        isManual = isManual == 1,
        franchiseName = franchiseName,
        franchiseOrder = franchiseOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

@Entity(
    tableName = "list_games",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["list_id", "game_id"], unique = true),
        Index(value = ["list_id"]),
        Index(value = ["game_id"]),
    ],
)
data class ListGameEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "list_id") val listId: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    val status: String,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "list_name") val listName: String,
) {
    fun toDomain(game: Game): ListEntry = ListEntry(
        id = id,
        listId = listId,
        game = game,
        status = GameStatus.valueOf(status),
        addedAt = addedAt,
    )
}

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String,
) {
    fun toThemeId(): com.backlogorder.domain.model.ThemeId = 
        try {
            com.backlogorder.domain.model.ThemeId.valueOf(value)
        } catch (e: IllegalArgumentException) {
            com.backlogorder.domain.model.ThemeId.VIOLET
        }
}

@Entity(
    tableName = "export_list_games",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
)
data class ExportListGameEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "list_id") val listId: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    @ColumnInfo(name = "list_name") val listName: String,
)
