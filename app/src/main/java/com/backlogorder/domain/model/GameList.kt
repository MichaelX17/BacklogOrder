package com.backlogorder.domain.model

data class GameList(
    val id: String,
    val name: String,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
