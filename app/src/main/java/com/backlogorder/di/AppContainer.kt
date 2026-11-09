package com.backlogorder.di

import android.content.Context
import androidx.room.Room
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

interface AppContainer {
    val database: BacklogOrderDatabase
    val apiKeyProvider: ApiKeyProvider
    val settingsRepository: SettingsRepository
    val listsRepository: ListsRepository
    val gamesRepository: GamesRepository
    val rawgApiService: RawgApiService
    val rawgRemoteDataSource: RawgRemoteDataSource
    val appDispatchers: AppDispatchers
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: BacklogOrderDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            BacklogOrderDatabase::class.java,
            "backlogorder.db",
        ).build()
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
        RoomGamesRepository(database)
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
}
