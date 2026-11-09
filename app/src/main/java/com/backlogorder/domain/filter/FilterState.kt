package com.backlogorder.domain.filter

import com.backlogorder.domain.model.GameStatus

data class FilterState(
    val name: String = "",
    val playtimeMin: Double? = null,
    val playtimeMax: Double? = null,
    val ratingMin: Double? = null,
    val ratingMax: Double? = null,
    val scoreMin: Double? = null,
    val scoreMax: Double? = null,
    val statuses: Set<GameStatus> = emptySet(),
)
