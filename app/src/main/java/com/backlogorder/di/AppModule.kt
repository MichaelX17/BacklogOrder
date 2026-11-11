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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BacklogOrderDatabase =
        BacklogOrderDatabase.create(context)

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
    fun provideGamesRepository(
        @ApplicationContext context: Context,
        database: BacklogOrderDatabase,
    ): GamesRepository =
        RoomGamesRepository(database, context = context)

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

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(dispatchers: AppDispatchers): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.io)

    @Provides
    @Singleton
    fun provideCoverImageDownloader(@ApplicationContext context: Context): CoverImageDownloader =
        CoverImageDownloader(context)

    @Provides
    @Singleton
    fun provideCoverRefresher(
        downloader: CoverImageDownloader,
        gamesRepository: GamesRepository,
        @ApplicationScope scope: CoroutineScope,
    ): CoverRefresher =
        CoverRefresher(
            downloader = downloader,
            gamesRepository = gamesRepository as RoomGamesRepository,
            scope = scope,
        )
}
