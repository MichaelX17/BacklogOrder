package com.backlogorder.domain.model

data class NewGame(
    val rawgId: Int? = null,
    val name: String,
    val backgroundImage: String? = null,
    val metacritic: Int? = null,
    val rating: Double? = null,
    val playtimeHours: Double? = null,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isManual: Boolean = false,
    val franchiseName: String? = null,
    val franchiseOrder: Int? = null,
) {
    fun toGame(id: String, createdAt: Long = 0L, updatedAt: Long = 0L): Game = Game(
        id = id,
        rawgId = rawgId,
        name = name,
        backgroundImage = backgroundImage,
        metacritic = metacritic,
        rating = rating,
        playtimeHours = playtimeHours,
        genres = genres,
        platforms = platforms,
        tags = tags,
        isManual = isManual,
        franchiseName = franchiseName,
        franchiseOrder = franchiseOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
