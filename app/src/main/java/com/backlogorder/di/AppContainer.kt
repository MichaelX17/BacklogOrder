package com.backlogorder.di

import android.content.Context
import com.backlogorder.data.local.BacklogOrderDatabase
import com.backlogorder.data.remote.RawgApiFactory
import com.backlogorder.data.remote.RawgApiService
import com.backlogorder.data.remote.RawgRemoteDataSource
import com.backlogorder.data.repository.RoomGamesRepository
import com.backlogorder.data.repository.RoomListsRepository
import com.backlogorder.data.repository.RoomSettingsRepository
import com.backlogorder.data.secure.EncryptedApiKeyProvider
import com.backlogorder.domain.repository.ApiKeyProvider
import com.backlogorder.domain.repository.GamesRepository
import com.backlogorder.domain.repository.ListsRepository
import com.backlogorder.domain.repository.SettingsRepository
import com.backlogorder.util.CoverImageDownloader
import com.backlogorder.util.CoverRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

interface AppContainer {
    val database: BacklogOrderDatabase
    val apiKeyProvider: ApiKeyProvider
    val settingsRepository: SettingsRepository
    val listsRepository: ListsRepository
    val gamesRepository: GamesRepository
    val rawgApiService: RawgApiService
    val rawgRemoteDataSource: RawgRemoteDataSource
    val appDispatchers: AppDispatchers
    val applicationScope: CoroutineScope
    val coverImageDownloader: CoverImageDownloader
    val coverRefresher: CoverRefresher
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: BacklogOrderDatabase by lazy {
        BacklogOrderDatabase.create(context.applicationContext)
    }

    override val apiKeyProvider: ApiKeyProvider by lazy {
        EncryptedApiKeyProvider(context.applicationContext)
    }

    override val settingsRepository: SettingsRepository by lazy {
        RoomSettingsRepository(context.applicationContext, database, apiKeyProvider, appDispatchers)
    }

    override val listsRepository: ListsRepository by lazy {
        RoomListsRepository(database)
    }

    override val gamesRepository: GamesRepository by lazy {
        RoomGamesRepository(database, context = context.applicationContext)
    }

    override val rawgApiService: RawgApiService by lazy {
        RawgApiFactory.create("https://api.rawg.io/api/") {
            apiKeyProvider.currentKey
        }
    }

    override val rawgRemoteDataSource: RawgRemoteDataSource by lazy {
        RawgRemoteDataSource(rawgApiService, dispatchers = appDispatchers)
    }

    override val appDispatchers: AppDispatchers by lazy {
        AppDispatchers()
    }

    override val applicationScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + appDispatchers.io)
    }

    override val coverImageDownloader: CoverImageDownloader by lazy {
        CoverImageDownloader(context.applicationContext)
    }

    override val coverRefresher: CoverRefresher by lazy {
        CoverRefresher(
            downloader = coverImageDownloader,
            gamesRepository = gamesRepository as RoomGamesRepository,
            scope = applicationScope,
        )
    }
}
