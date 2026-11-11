package com.backlogorder.domain.mapper

import com.backlogorder.domain.model.NewGame
import com.backlogorder.domain.model.RawgGame

object RawgMapper {
    fun mapRawgGame(rawgGame: RawgGame): NewGame {
        return NewGame(
            rawgId = rawgGame.rawgId,
            name = rawgGame.name,
            backgroundImage = null,
            backgroundImageUrl = rawgGame.backgroundImage,
            metacritic = rawgGame.metacritic,
            rating = rawgGame.rating,
            playtimeHours = rawgGame.playtimeHours,
            genres = rawgGame.genres,
            platforms = rawgGame.platforms,
            tags = rawgGame.tags,
            isManual = false,
            franchiseName = null,
            franchiseOrder = null,
        )
    }
}
