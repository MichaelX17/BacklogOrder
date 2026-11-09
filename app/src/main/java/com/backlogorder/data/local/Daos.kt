package com.backlogorder.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    @Query("SELECT * FROM lists ORDER BY created_at DESC")
    fun observeLists(): Flow<List<ListEntity>>

    @Upsert
    suspend fun insertList(entity: ListEntity): Long

    @Update
    suspend fun updateList(entity: ListEntity)

    @Query("DELETE FROM lists WHERE id = :id")
    suspend fun deleteList(id: String)

    @Query("DELETE FROM lists")
    suspend fun deleteAllLists()
}

@Dao
interface GamesDao {
    @Query("SELECT * FROM games WHERE id IN (:ids)")
    suspend fun getGamesByIds(ids: List<String>): List<GameEntity>

    @Query("SELECT * FROM games WHERE rawg_id = :rawgId LIMIT 1")
    suspend fun findByRawgId(rawgId: Int): GameEntity?

    @Query("SELECT * FROM games WHERE is_manual = 1 AND lower(name) = lower(:name) LIMIT 1")
    suspend fun findManualByName(name: String): GameEntity?

    @Query("""
        SELECT lg.* FROM list_games lg
        INNER JOIN games g ON lg.game_id = g.id
        WHERE lg.list_id = :listId
        ORDER BY lg.added_at ASC
    """)
    fun observeEntriesForList(listId: String): Flow<List<ListGameEntity>>

    @Query("""
        SELECT lg.* FROM list_games lg
        INNER JOIN games g ON lg.game_id = g.id
        ORDER BY lg.added_at ASC
    """)
    fun observeAllEntries(): Flow<List<ListGameEntity>>

    @Upsert
    suspend fun insertGame(entity: GameEntity): Long

    @Upsert
    suspend fun upsertGame(entity: GameEntity)

    @Upsert
    suspend fun insertListGame(entity: ListGameEntity): Long

    @Query("SELECT * FROM list_games WHERE list_id = :listId AND game_id = :gameId LIMIT 1")
    suspend fun findListGame(listId: String, gameId: String): ListGameEntity?

    @Query("DELETE FROM list_games WHERE list_id = :listId AND game_id = :gameId")
    suspend fun removeGameFromList(listId: String, gameId: String)

    @Query("UPDATE list_games SET status = :status WHERE list_id = :listId AND game_id = :gameId")
    suspend fun updateStatus(listId: String, gameId: String, status: String)

    @Query("UPDATE games SET franchise_name = :name, franchise_order = :order WHERE id = :gameId")
    suspend fun setFranchise(gameId: String, name: String?, order: Int?)

    @Query("SELECT * FROM games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): GameEntity?

    @Query("SELECT * FROM games")
    suspend fun getAllGames(): List<GameEntity>

    @Query("SELECT * FROM list_games WHERE list_id = :listId")
    suspend fun listGamesFor(listId: String): List<ListGameEntity>

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()

    @Query("DELETE FROM list_games")
    suspend fun deleteAllListGames()
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    fun observeValue(key: String): Flow<String?>

    @Upsert
    suspend fun upsert(entity: SettingsEntity)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun clear(key: String)
    
    // Settings exports
    @Query("SELECT value FROM settings WHERE `key` = 'onboarding_complete'")
    fun observeOnboardingComplete(): Flow<String?>
    
    @Query("SELECT value FROM settings WHERE `key` = 'theme'")
    fun observeTheme(): Flow<String?>

    @Query("SELECT value FROM settings WHERE `key` = 'dark_mode'")
    fun observeDarkMode(): Flow<String?>

    @Query("SELECT value FROM settings WHERE `key` = 'custom_color_hex'")
    fun observeCustomColorHex(): Flow<String?>
    
    suspend fun setOnboardingComplete(value: Boolean) {
        upsert(SettingsEntity(key = "onboarding_complete", value = value.toString()))
    }
    
    suspend fun setTheme(themeId: String) {
        upsert(SettingsEntity(key = "theme", value = themeId))
    }

    suspend fun setDarkMode(value: Boolean) {
        upsert(SettingsEntity(key = "dark_mode", value = value.toString()))
    }

    suspend fun setCustomColorHex(hex: String) {
        upsert(SettingsEntity(key = "custom_color_hex", value = hex))
    }
}
