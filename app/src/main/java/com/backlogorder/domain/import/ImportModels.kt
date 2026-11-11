package com.backlogorder.domain.import

import com.backlogorder.data.local.GameEntity
import com.backlogorder.data.local.ListEntity
import com.backlogorder.data.local.ListGameEntity
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.ExportedGame
import com.backlogorder.domain.ExportedList
import com.backlogorder.domain.ExportedListGame

enum class ImportMode {
    MERGE,
    RESTORE,
}

enum class ConflictResolution {
    KEEP_LOCAL,
    KEEP_BACKUP,
    MERGE_FIELDS,
    USE_LOCAL,
    USE_BACKUP,
}

data class FieldMergeSelection(
    val useLocalName: Boolean = true,
    val useLocalMetacritic: Boolean = true,
    val useLocalRating: Boolean = true,
    val useMaxPlaytimeHours: Boolean = true,
    val useUnionGenres: Boolean = true,
    val useUnionPlatforms: Boolean = true,
    val useUnionTags: Boolean = true,
    val useOrIsManual: Boolean = true,
    val useLocalFranchise: Boolean = true,
    val useMinCreatedAt: Boolean = true,
    val useMaxUpdatedAt: Boolean = true,
    val useLocalCoverImage: Boolean = true,
    val useLocalCoverUrl: Boolean = true,
)

data class LocalSnapshot(
    val lists: Map<String, ListEntity> = emptyMap(),
    val games: Map<String, GameEntity> = emptyMap(),
    val gamesByRawgId: Map<Int, GameEntity> = emptyMap(),
    val listGames: Map<String, ListGameEntity> = emptyMap(),
    val listGamesByPair: Map<Pair<String, String>, ListGameEntity> = emptyMap(),
)

sealed interface ImportConflict {
    val backupItemId: String

    data class GameMetadata(
        override val backupItemId: String,
        val local: GameEntity,
        val backup: ExportedGame,
    ) : ImportConflict

    data class GameRawgId(
        override val backupItemId: String,
        val local: GameEntity,
        val backup: ExportedGame,
    ) : ImportConflict

    data class ListName(
        override val backupItemId: String,
        val local: ListEntity,
        val backup: ExportedList,
    ) : ImportConflict

    data class Membership(
        override val backupItemId: String,
        val local: ListGameEntity,
        val backup: ExportedListGame,
    ) : ImportConflict
}

data class ImportPlan(
    val toAddLists: List<ListEntity> = emptyList(),
    val toAddGames: List<GameEntity> = emptyList(),
    val toAddListGames: List<ListGameEntity> = emptyList(),
    val toUpdateLists: List<ListEntity> = emptyList(),
    val toUpdateGames: List<GameEntity> = emptyList(),
    val toUpdateListGames: List<ListGameEntity> = emptyList(),
    val conflicts: List<ImportConflict> = emptyList(),
    val skippedLists: Int = 0,
    val skippedGames: Int = 0,
    val skippedMemberships: Int = 0,
    val missingCoversSkipped: Int = 0,
    val backupToEffectiveGameId: Map<String, String> = emptyMap(),
) {
    val isNoOp: Boolean
        get() = toAddLists.isEmpty() &&
            toAddGames.isEmpty() &&
            toAddListGames.isEmpty() &&
            toUpdateLists.isEmpty() &&
            toUpdateGames.isEmpty() &&
            toUpdateListGames.isEmpty() &&
            conflicts.isEmpty()
}

data class ImportReport(
    val mode: ImportMode,
    val addedLists: Int = 0,
    val addedGames: Int = 0,
    val addedMemberships: Int = 0,
    val updatedLists: Int = 0,
    val updatedGames: Int = 0,
    val updatedMemberships: Int = 0,
    val skippedLists: Int = 0,
    val skippedGames: Int = 0,
    val skippedMemberships: Int = 0,
    val missingCoversSkipped: Int = 0,
    val snapshotPath: String? = null,
)

sealed interface ImportResultState {
    data object Idle : ImportResultState
    data class Parsing(val filePath: String) : ImportResultState
    data class AwaitingMode(val backupData: ExportData) : ImportResultState
    data class BuildingPlan(val mode: ImportMode) : ImportResultState
    data class Preview(val plan: ImportPlan, val mode: ImportMode, val backupData: ExportData) : ImportResultState
    data class ResolvingConflicts(
        val plan: ImportPlan,
        val mode: ImportMode,
        val backupData: ExportData,
        val resolutions: Map<String, ConflictResolution>,
        val fieldSelections: Map<String, FieldMergeSelection> = emptyMap(),
    ) : ImportResultState
    data class Applying(val mode: ImportMode) : ImportResultState
    data class Done(val report: ImportReport) : ImportResultState
    data class Error(val message: String, val cause: Throwable? = null) : ImportResultState
}
