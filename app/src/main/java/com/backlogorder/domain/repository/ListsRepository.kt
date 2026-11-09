package com.backlogorder.domain.repository

import com.backlogorder.domain.model.GameList
import kotlinx.coroutines.flow.Flow

interface ListsRepository {
    fun observeLists(): Flow<List<GameList>>
    suspend fun createList(name: String): GameList
    suspend fun renameList(id: String, name: String)
    suspend fun deleteList(id: String)
}
