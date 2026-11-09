package com.backlogorder.domain.model

data class RawgGame(
    val rawgId: Int,
    val name: String,
    val backgroundImage: String? = null,
    val metacritic: Int? = null,
    val rating: Double? = null,
    val playtimeHours: Double? = null,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)
