package com.backlogorder.data.remote

import com.backlogorder.di.AppDispatchers
import com.backlogorder.domain.error.RawgErrorMapper
import com.backlogorder.domain.model.RawgGame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

class RawgSearchCache(
    private val ttlMs: Long = 30_000L,
    private val maxEntries: Int = 20,
) {
    private val cache = object : LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>): Boolean {
            return size > maxEntries
        }
    }

    @Synchronized
    fun get(key: String): List<RawgGame>? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestampMs > ttlMs) {
            cache.remove(key)
            return null
        }
        return entry.games
    }

    @Synchronized
    fun put(key: String, games: List<RawgGame>) {
        cache[key] = CacheEntry(games = games, timestampMs = System.currentTimeMillis())
    }

    private data class CacheEntry(
        val games: List<RawgGame>,
        val timestampMs: Long,
    )
}

class RawgRemoteDataSource(
    private val apiService: RawgApiService,
    private val errorMapper: RawgErrorMapper = RawgErrorMapper(),
    private val cache: RawgSearchCache = RawgSearchCache(),
    private val dispatchers: AppDispatchers = AppDispatchers(),
) {
    suspend fun searchGames(query: String, apiKey: String): List<RawgGame> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return emptyList()

        val cacheKey = "${normalizedQuery.lowercase()}|$apiKey"
        cache.get(cacheKey)?.let { return it }

        return withContext(dispatchers.io) {
            try {
                val results = apiService.searchGames(normalizedQuery, apiKey).results.map { it.toDomain() }
                cache.put(cacheKey, results)
                results
            } catch (exception: Exception) {
                throw mapRemoteException(exception)
            }
        }
    }

    suspend fun getGameDetails(gameId: Int, apiKey: String): RawgGame {
        return withContext(dispatchers.io) {
            try {
                apiService.getGame(gameId, apiKey).toDomain()
            } catch (exception: Exception) {
                throw mapRemoteException(exception)
            }
        }
    }

    private fun mapRemoteException(exception: Throwable): Nothing {
        if (exception is CancellationException) {
            throw exception
        }

        val error = when (exception) {
            is HttpException -> errorMapper.map(exception.code(), exception)
            is TimeoutException, is SocketTimeoutException -> errorMapper.map(throwable = exception)
            is IOException -> errorMapper.map(throwable = exception)
            else -> errorMapper.map(throwable = exception)
        }
        throw error
    }
}
