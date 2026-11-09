package com.backlogorder.data.remote

import com.backlogorder.domain.error.AppError
import com.backlogorder.domain.model.RawgGame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeoutException

class RawgRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var dataSource: RawgRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = RawgApiFactory.create(server.url("/").toString(), apiKeyProvider = { "demo-key" })
        dataSource = RawgRemoteDataSource(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun searchGames_parsesAndCachesResultsAndSendsApiKeyHeader() {
        runBlocking {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(
                        """
                        {
                          "results": [
                            {
                              "id": 42,
                              "name": "Portal",
                              "background_image": "https://example.com/portal.jpg",
                              "metacritic": 95,
                              "rating": 4.8,
                              "playtime": 7.5,
                              "genres": [{"name": "Puzzle"}],
                              "platforms": [{"platform": {"name": "PC"}}],
                              "tags": [{"name": "singleplayer"}]
                            }
                          ]
                        }
                        """.trimIndent(),
                    ),
            )

            val first = dataSource.searchGames("portal", "demo-key")
            val second = dataSource.searchGames("portal", "demo-key")

            assertEquals(1, first.size)
            assertEquals(42, first[0].rawgId)
            assertEquals("Portal", first[0].name)
            assertEquals("Puzzle", first[0].genres.single())
            assertEquals(1, server.requestCount)
            val request = server.takeRequest()
            assertEquals("demo-key", request.getHeader("X-API-Key"))
            assertEquals("BacklogOrder/1.0", request.getHeader("User-Agent"))
            assertEquals(1, second.size)
        }
    }

    @Test
    fun searchGames_mapsHttpStatusesToDomainErrors() {
        runBlocking {
            server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))
            val unauthorized = assertThrows(AppError.Unauthorized::class.java) {
                runBlocking { dataSource.searchGames("portal", "demo-key") }
            }
            assertTrue(unauthorized.message!!.lowercase().contains("unauthorized"))

            server.enqueue(MockResponse().setResponseCode(404).setBody("{}"))
            val notFound = assertThrows(AppError.NotFound::class.java) {
                runBlocking { dataSource.searchGames("portal", "demo-key") }
            }
            assertEquals("Not found", notFound.message)

            server.enqueue(MockResponse().setResponseCode(429).setBody("{}"))
            val rateLimited = assertThrows(AppError.RateLimited::class.java) {
                runBlocking { dataSource.searchGames("portal", "demo-key") }
            }
            assertEquals("Rate limited", rateLimited.message)
        }
    }

    @Test
    fun searchGames_mapsTimeoutToAppError() {
        runBlocking {
            val service = object : RawgApiService {
                override suspend fun searchGames(
                    query: String,
                    apiKey: String,
                    pageSize: Int,
                ): RawgSearchResponse {
                    throw TimeoutException("slow")
                }

                override suspend fun getGame(gameId: Int, apiKey: String): RawgGameDto {
                    throw TimeoutException("slow")
                }
            }

            val source = RawgRemoteDataSource(service)
            val failure = assertThrows(AppError.Timeout::class.java) {
                runBlocking { source.searchGames("portal", "demo-key") }
            }
            assertTrue(failure.message!!.contains("timed out"))
        }
    }

    @Test
    fun searchGames_propagatesCancellation() {
        runBlocking {
            val service = object : RawgApiService {
                override suspend fun searchGames(
                    query: String,
                    apiKey: String,
                    pageSize: Int,
                ): RawgSearchResponse {
                    throw CancellationException("cancelled")
                }

                override suspend fun getGame(gameId: Int, apiKey: String): RawgGameDto {
                    throw CancellationException("cancelled")
                }
            }

            val source = RawgRemoteDataSource(service)
            assertThrows(CancellationException::class.java) {
                runBlocking { source.searchGames("portal", "demo-key") }
            }
        }
    }

    @Test
    fun rawgSearchCache_capsAt20Entries() {
        val cache = RawgSearchCache(ttlMs = 60_000L, maxEntries = 20)
        for (i in 1..30) {
            cache.put("key-$i", listOf(RawgGame(rawgId = i, name = "Game $i")))
        }
        var presentCount = 0
        for (i in 1..30) {
            if (cache.get("key-$i") != null) {
                presentCount++
            }
        }
        assertEquals(20, presentCount)
    }
}
