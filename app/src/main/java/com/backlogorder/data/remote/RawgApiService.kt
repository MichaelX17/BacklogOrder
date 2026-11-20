package com.backlogorder.data.remote

import com.backlogorder.domain.model.RawgGame
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("search") query: String,
        @Query("key") apiKey: String,
        @Query("page_size") pageSize: Int = 20,
    ): RawgSearchResponse

    @GET("games/{gameId}")
    suspend fun getGame(
        @Path("gameId") gameId: Int,
        @Query("key") apiKey: String,
    ): RawgGameDto
}

@Serializable
data class RawgSearchResponse(
    val results: List<RawgGameDto>? = null,
)

@Serializable
data class RawgGameDto(
    @SerialName("id") val rawgId: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("background_image") val backgroundImage: String? = null,
    @SerialName("metacritic") val metacritic: Int? = null,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("playtime") val playtimeHours: Double? = null,
    @SerialName("genres") val genres: List<RawgGenreDto>? = null,
    @SerialName("platforms") val platforms: List<RawgPlatformWrapperDto>? = null,
    @SerialName("tags") val tags: List<RawgTagDto>? = null,
) {
    fun toDomain(): RawgGame = RawgGame(
        rawgId = rawgId,
        name = name,
        backgroundImage = backgroundImage,
        metacritic = metacritic,
        rating = rating,
        playtimeHours = playtimeHours,
        genres = genres?.mapNotNull { it.name } ?: emptyList(),
        platforms = platforms?.mapNotNull { it.platform?.name } ?: emptyList(),
        tags = tags?.mapNotNull { it.name } ?: emptyList(),
    )
}

@Serializable
data class RawgGenreDto(
    @SerialName("name") val name: String? = null,
)

@Serializable
data class RawgPlatformWrapperDto(
    @SerialName("platform") val platform: RawgPlatformDto? = null,
)

@Serializable
data class RawgPlatformDto(
    @SerialName("name") val name: String? = null,
)

@Serializable
data class RawgTagDto(
    @SerialName("name") val name: String? = null,
)
