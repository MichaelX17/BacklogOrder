package com.backlogorder.domain.repository

import com.backlogorder.domain.model.Game
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.ListEntry
import com.backlogorder.domain.model.NewGame
import kotlinx.coroutines.flow.Flow

interface GamesRepository {
    fun observeEntriesForList(listId: String): Flow<List<ListEntry>>
    fun observeAllEntries(): Flow<List<ListEntry>>
    suspend fun addGameToList(listId: String, game: NewGame, status: GameStatus): ListEntry
    suspend fun removeGameFromList(listId: String, gameId: String)
    suspend fun updateStatus(listId: String, gameId: String, status: GameStatus)
    suspend fun updateGame(game: Game)
    suspend fun setFranchise(gameId: String, name: String?, order: Int?)
}
