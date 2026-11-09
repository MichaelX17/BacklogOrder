package com.backlogorder.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.StringListConverter
import com.backlogorder.data.repository.RoomGamesRepository
import com.backlogorder.data.repository.RoomListsRepository
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.model.NewGame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDataModelTest {
    private lateinit var database: BacklogOrderDatabase
    private lateinit var gamesRepository: RoomGamesRepository
    private lateinit var listsRepository: RoomListsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BacklogOrderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gamesRepository = RoomGamesRepository(database)
        listsRepository = RoomListsRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun stringListConverter_roundTripsJsonLists() {
        val converter = StringListConverter()
        val input = listOf("Strategy", "RPG", "Indie")

        assertEquals(input, converter.fromString(converter.toString(input)))
    }

    @Test
    fun listEntity_mapsToDomainModel() {
        val entity = ListEntity(
            id = "list-1",
            name = "Current",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )

        val domain = entity.toDomain()

        assertEquals("list-1", domain.id)
        assertEquals("Current", domain.name)
        assertEquals(1_000L, domain.createdAt)
        assertEquals(2_000L, domain.updatedAt)
    }

    @Test
    fun deleteList_removesListLinksButKeepsSharedGames() = runBlocking {
        val listA = listsRepository.createList("List A")
        val listB = listsRepository.createList("List B")
        val first = gamesRepository.addGameToList(
            listA.id,
            NewGame(rawgId = 42, name = "Shared Game"),
            GameStatus.BACKLOG,
        )
        gamesRepository.addGameToList(
            listB.id,
            NewGame(rawgId = 42, name = "Shared Game"),
            GameStatus.PLAYING,
        )

        listsRepository.deleteList(listA.id)

        assertTrue(database.gamesDao().listGamesFor(listA.id).isEmpty())
        assertEquals(1, database.gamesDao().listGamesFor(listB.id).size)
        assertNotNull(database.gamesDao().getGameById(first.game.id))
    }

    @Test
    fun addGameToList_dedupesByRawgId() = runBlocking {
        val list = listsRepository.createList("List")

        val first = gamesRepository.addGameToList(
            list.id,
            NewGame(rawgId = 101, name = "Kart Racer"),
            GameStatus.BACKLOG,
        )
        val second = gamesRepository.addGameToList(
            list.id,
            NewGame(rawgId = 101, name = "Kart Racer"),
            GameStatus.PLAYING,
        )

        assertEquals(first.game.id, second.game.id)
        assertEquals(1, database.gamesDao().listGamesFor(list.id).size)
        assertEquals(GameStatus.PLAYING.name, database.gamesDao().findListGame(list.id, first.game.id)?.status)
    }

    @Test
    fun addGameToList_dedupesManualGamesByNameCaseInsensitive() = runBlocking {
        val list = listsRepository.createList("Manual")

        val first = gamesRepository.addGameToList(
            list.id,
            NewGame(name = "Portal", isManual = true),
            GameStatus.BACKLOG,
        )
        val second = gamesRepository.addGameToList(
            list.id,
            NewGame(name = "portal", isManual = true),
            GameStatus.COMPLETED,
        )

        assertEquals(first.game.id, second.game.id)
        assertEquals(1, database.gamesDao().listGamesFor(list.id).size)
        assertEquals(GameStatus.COMPLETED.name, database.gamesDao().findListGame(list.id, first.game.id)?.status)
    }

    @Test
    fun updateStatus_persistsStatusChange() = runBlocking {
        val list = listsRepository.createList("Status list")
        val entry = gamesRepository.addGameToList(
            list.id,
            NewGame(name = "Status game", isManual = true),
            GameStatus.BACKLOG,
        )

        gamesRepository.updateStatus(list.id, entry.game.id, GameStatus.DROPPED)

        assertEquals(GameStatus.DROPPED.name, database.gamesDao().findListGame(list.id, entry.game.id)?.status)
    }

    @Test
    fun observeAllEntries_matchesPerListObservables() = runBlocking {
        val listA = listsRepository.createList("A")
        val listB = listsRepository.createList("B")
        val aEntry = gamesRepository.addGameToList(listA.id, NewGame(rawgId = 1, name = "One"), GameStatus.BACKLOG)
        val bEntry = gamesRepository.addGameToList(listB.id, NewGame(rawgId = 2, name = "Two"), GameStatus.PLAYING)

        val listAEntries = gamesRepository.observeEntriesForList(listA.id).first()
        val listBEntries = gamesRepository.observeEntriesForList(listB.id).first()
        val allEntries = gamesRepository.observeAllEntries().first()

        assertEquals(1, listAEntries.size)
        assertEquals(1, listBEntries.size)
        assertEquals(2, allEntries.size)
        assertTrue(allEntries.any { it.game.id == aEntry.game.id })
        assertTrue(allEntries.any { it.id == bEntry.id })
    }

    @Test
    fun importData_isIdempotentAndRollsBackOnInvalidPayload() = runBlocking {
        val payload = """
            {
              "version": 1,
              "lists": [{"id":"list-1","name":"Imported List","createdAt":1,"updatedAt":1}],
              "games": [{"id":"game-1","rawgId":77,"name":"Imported Game","backgroundImage":null,"metacritic":null,"rating":null,"playtimeHours":null,"genres":[],"platforms":[],"tags":[],"isManual":false,"franchiseName":null,"franchiseOrder":null,"createdAt":1,"updatedAt":1}],
              "listGames": [{"id":"link-1","listId":"list-1","gameId":"game-1","status":"BACKLOG","addedAt":2}]
            }
        """.trimIndent()

        gamesRepository.importData(payload)
        gamesRepository.importData(payload)

        assertEquals(1, database.gamesDao().listGamesFor("list-1").size)
        assertEquals(1, database.listDao().observeLists().first().size)

        val badPayload = "{\"version\":999,\"lists\":[],\"games\":[],\"listGames\":[] }"
        val expectedException = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { gamesRepository.importData(badPayload) }
        }
        assertTrue(expectedException.message.orEmpty().contains("Unsupported export version"))

        assertEquals(1, database.gamesDao().listGamesFor("list-1").size)
    }
}
