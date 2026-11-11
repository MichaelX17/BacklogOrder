package com.backlogorder.domain.import

import android.net.Uri
import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.ListGameEntity
import com.backlogorder.data.local.StringListConverter
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.ExportedGame
import com.backlogorder.domain.GameContentHasher
import java.io.File

object MergePlanBuilder {

    fun buildMergePlan(
        backup: ExportData,
        local: LocalSnapshot,
        localFileChecker: (String?) -> Boolean = ::defaultLocalFileChecker,
    ): ImportPlan {
        val stringConverter = StringListConverter()

        val toAddLists = mutableListOf<ListEntity>()
        val toAddGames = mutableListOf<GameEntity>()
        val toAddListGames = mutableListOf<ListGameEntity>()
        val conflicts = mutableListOf<ImportConflict>()

        var skippedLists = 0
        var skippedGames = 0
        var skippedMemberships = 0
        var missingCoversSkipped = 0

        val backupToEffectiveGameId = mutableMapOf<String, String>()

        // Step A: Games identity & conflicts
        for (backupGame in backup.games) {
            val gameIdStr = backupGame.id.toString()
            val localByUuid = local.games[gameIdStr]
            val localByRawgId = if (backupGame.rawgId != null) local.gamesByRawgId[backupGame.rawgId] else null

            when {
                localByUuid != null -> {
                    backupToEffectiveGameId[gameIdStr] = gameIdStr
                    if (isSameContent(localByUuid, backupGame, stringConverter)) {
                        skippedGames++
                    } else {
                        conflicts += ImportConflict.GameMetadata(gameIdStr, localByUuid, backupGame)
                    }
                }

                localByRawgId != null -> {
                    backupToEffectiveGameId[gameIdStr] = localByRawgId.id
                    if (isSameContent(localByRawgId, backupGame, stringConverter)) {
                        skippedGames++
                    } else {
                        conflicts += ImportConflict.GameRawgId(gameIdStr, localByRawgId, backupGame)
                    }
                }

                else -> {
                    backupToEffectiveGameId[gameIdStr] = gameIdStr
                    var coverPath = backupGame.backgroundImage
                    if (coverPath != null && !localFileChecker(coverPath)) {
                        coverPath = null
                        missingCoversSkipped++
                    }

                    toAddGames += GameEntity(
                        id = gameIdStr,
                        rawgId = backupGame.rawgId,
                        name = backupGame.gameName,
                        backgroundImage = coverPath,
                        backgroundImageUrl = backupGame.backgroundImageUrl,
                        metacritic = backupGame.metacritic,
                        rating = backupGame.rating,
                        playtimeHours = backupGame.playtimeHours,
                        genres = stringConverter.toString(backupGame.genres),
                        platforms = stringConverter.toString(backupGame.platforms),
                        tags = stringConverter.toString(backupGame.tags),
                        isManual = if (backupGame.isManual) 1 else 0,
                        franchiseName = backupGame.franchiseName,
                        franchiseOrder = backupGame.franchiseOrder,
                        createdAt = backupGame.createdAt,
                        updatedAt = backupGame.updatedAt,
                    )
                }
            }
        }

        // Step B: Lists identity & conflicts
        for (backupList in backup.lists) {
            val listIdStr = backupList.id.toString()
            val localList = local.lists[listIdStr]

            when {
                localList == null -> {
                    toAddLists += ListEntity(
                        id = listIdStr,
                        name = backupList.listName,
                        createdAt = backupList.createdAt,
                        updatedAt = backupList.updatedAt,
                    )
                }

                localList.name != backupList.listName -> {
                    conflicts += ImportConflict.ListName(listIdStr, localList, backupList)
                }

                else -> {
                    skippedLists++
                }
            }
        }

        // Step C: Memberships identity & conflicts
        for (backupLG in backup.listGames) {
            val lgIdStr = backupLG.id.toString()
            val listIdStr = backupLG.listId.toString()
            val effectiveGameId = backupToEffectiveGameId[backupLG.gameId.toString()]
                ?: error("Orphan reference in backup listGames: ${backupLG.gameId}")

            val localByUuid = local.listGames[lgIdStr]
            val localByPair = local.listGamesByPair[Pair(listIdStr, effectiveGameId)]

            when {
                localByUuid != null && localByPair?.id == localByUuid.id -> {
                    if (localByUuid.status == backupLG.status.name && localByUuid.addedAt == backupLG.addedAt) {
                        skippedMemberships++
                    } else {
                        conflicts += ImportConflict.Membership(lgIdStr, localByUuid, backupLG)
                    }
                }

                localByPair != null -> {
                    conflicts += ImportConflict.Membership(lgIdStr, localByPair, backupLG)
                }

                else -> {
                    val listName = local.lists[listIdStr]?.name
                        ?: backup.lists.find { it.id.toString() == listIdStr }?.listName
                        ?: ""

                    toAddListGames += ListGameEntity(
                        id = lgIdStr,
                        listId = listIdStr,
                        gameId = effectiveGameId,
                        status = backupLG.status.name,
                        addedAt = backupLG.addedAt,
                        listName = listName,
                    )
                }
            }
        }

        return ImportPlan(
            toAddLists = toAddLists,
            toAddGames = toAddGames,
            toAddListGames = toAddListGames,
            conflicts = conflicts,
            skippedLists = skippedLists,
            skippedGames = skippedGames,
            skippedMemberships = skippedMemberships,
            missingCoversSkipped = missingCoversSkipped,
            backupToEffectiveGameId = backupToEffectiveGameId,
        )
    }

    private fun isSameContent(
        local: GameEntity,
        backup: ExportedGame,
        stringConverter: StringListConverter,
    ): Boolean {
        val genres = stringConverter.fromString(local.genres)
        val platforms = stringConverter.fromString(local.platforms)
        val tags = stringConverter.fromString(local.tags)
        val localHash = GameContentHasher.computeHash(
            name = local.name,
            rawgId = local.rawgId,
            metacritic = local.metacritic,
            rating = local.rating,
            playtimeHours = local.playtimeHours,
            genres = genres,
            platforms = platforms,
            tags = tags,
            isManual = local.isManual == 1,
            franchiseName = local.franchiseName,
            franchiseOrder = local.franchiseOrder,
        )

        val backupHash = if (backup.contentHash.isNotBlank()) {
            backup.contentHash
        } else {
            GameContentHasher.computeHash(backup)
        }

        return localHash == backupHash
    }

    private fun defaultLocalFileChecker(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return try {
            val parsedPath = if (path.startsWith("file://", ignoreCase = true)) {
                Uri.parse(path).path ?: path
            } else {
                path
            }
            val file = File(parsedPath)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }
}
