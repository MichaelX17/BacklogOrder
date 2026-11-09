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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BacklogOrderDatabase =
        Room.databaseBuilder(
            context,
            BacklogOrderDatabase::class.java,
            "backlogorder.db",
        ).build()

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        database: BacklogOrderDatabase,
        apiKeyProvider: ApiKeyProvider,
        dispatchers: AppDispatchers,
    ): SettingsRepository =
        RoomSettingsRepository(context, database, apiKeyProvider, dispatchers)

    @Provides
    @Singleton
    fun provideListsRepository(database: BacklogOrderDatabase): ListsRepository =
        RoomListsRepository(database)

    @Provides
    @Singleton
    fun provideGamesRepository(database: BacklogOrderDatabase): GamesRepository =
        RoomGamesRepository(database)

    @Provides
    @Singleton
    fun provideApiKeyProvider(@ApplicationContext context: Context): ApiKeyProvider =
        EncryptedApiKeyProvider(context)

    @Provides
    @Singleton
    fun provideRawgApiService(apiKeyProvider: ApiKeyProvider): RawgApiService =
        RawgApiFactory.create("https://api.rawg.io/api/") {
            apiKeyProvider.currentKey
        }

    @Provides
    @Singleton
    fun provideRawgRemoteDataSource(
        apiService: RawgApiService,
        dispatchers: AppDispatchers,
    ): RawgRemoteDataSource =
        RawgRemoteDataSource(apiService, dispatchers = dispatchers)

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = AppDispatchers()
}
