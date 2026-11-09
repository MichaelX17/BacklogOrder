package com.backlogorder.data.repository

import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.ListEntity
import com.backlogorder.domain.model.GameList
import com.backlogorder.domain.repository.ListsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomListsRepository(
    private val database: BacklogOrderDatabase,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ListsRepository {
    override fun observeLists(): Flow<List<GameList>> = database.listDao().observeLists().map { list ->
        list.map(ListEntity::toDomain)
    }

    override suspend fun createList(name: String): GameList {
        val now = clock()
        val entity = ListEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = now,
            updatedAt = now,
        )
        database.listDao().insertList(entity)
        return entity.toDomain()
    }

    override suspend fun renameList(id: String, name: String) {
        val current = database.listDao().observeLists().firstOrNull()?.firstOrNull { it.id == id }
        current?.let { entity ->
            database.listDao().updateList(
                entity.copy(name = name, updatedAt = clock()),
            )
        }
    }

    override suspend fun deleteList(id: String) {
        database.listDao().deleteList(id)
    }
}
