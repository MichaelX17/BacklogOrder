package com.backlogorder.domain.model

data class Game(
    val id: String,
    val rawgId: Int? = null,
    val name: String,
    val backgroundImage: String? = null,
    val backgroundImageUrl: String? = null,
    val metacritic: Int? = null,
    val rating: Double? = null,
    val playtimeHours: Double? = null,
    val genres: List<String> = emptyList(),
    val platforms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val isManual: Boolean = false,
    val franchiseName: String? = null,
    val franchiseOrder: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
