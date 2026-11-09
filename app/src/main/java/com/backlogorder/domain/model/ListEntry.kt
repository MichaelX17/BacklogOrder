package com.backlogorder.domain.model

data class ListEntry(
    val id: String,
    val listId: String,
    val game: Game,
    val status: GameStatus,
    val addedAt: Long = 0L,
)
