package com.backlogorder.util

import com.backlogorder.data.repository.RoomGamesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoverRefresher @Inject constructor(
    private val downloader: CoverImageDownloader,
    private val gamesRepository: RoomGamesRepository,
    private val scope: CoroutineScope,
    private val cooldownMs: Long = 5 * 60 * 1000L, // 5 minutes
) {
    private val inFlight = mutableSetOf<String>()
    private val recentAttempts = mutableMapOf<String, Long>()

    @Synchronized
    fun requestRefresh(gameId: String) {
        val now = System.currentTimeMillis()
        if (gameId in inFlight) return
        if (now - (recentAttempts[gameId] ?: 0L) < cooldownMs) return

        inFlight += gameId
        recentAttempts[gameId] = now
        scope.launch {
            try {
                val game = gamesRepository.getGameById(gameId) ?: return@launch
                val url = game.backgroundImageUrl ?: return@launch
                val result = downloader.download(gameId, url)
                if (result.isSuccess) {
                    gamesRepository.updateCoverFields(
                        gameId = gameId,
                        localPath = result.getOrNull(),
                        url = url,
                    )
                }
            } finally {
                synchronized(this@CoverRefresher) {
                    inFlight -= gameId
                }
            }
        }
    }
}
