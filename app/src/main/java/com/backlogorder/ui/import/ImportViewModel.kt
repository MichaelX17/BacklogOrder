package com.backlogorder.ui.import

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.backlogorder.data.repository.RoomSettingsRepository
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.ValidationResult
import com.backlogorder.domain.import.ConflictResolution
import com.backlogorder.domain.import.FieldMergeSelection
import com.backlogorder.domain.import.ImportConflict
import com.backlogorder.domain.import.ImportMode
import com.backlogorder.domain.import.ImportPlan
import com.backlogorder.domain.import.ImportResultState
import com.backlogorder.domain.import.MergePlanBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ImportViewModel(
    private val settingsRepository: RoomSettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ImportResultState>(ImportResultState.Idle)
    val state: StateFlow<ImportResultState> = _state.asStateFlow()

    fun reset() {
        _state.value = ImportResultState.Idle
    }

    fun startImportFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = ImportResultState.Parsing(uri.toString())
            val content = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: return@launch run {
                    _state.value = ImportResultState.Error("Could not read file from selected URI")
                }
            } catch (e: Exception) {
                _state.value = ImportResultState.Error("Failed to read file: ${e.localizedMessage}", e)
                return@launch
            }

            parseAndValidate(content)
        }
    }

    fun startImportFromContent(jsonString: String) {
        viewModelScope.launch {
            _state.value = ImportResultState.Parsing("raw_content")
            parseAndValidate(jsonString)
        }
    }

    private fun parseAndValidate(jsonString: String) {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = false }
        val backupData = try {
            json.decodeFromString<ExportData>(jsonString)
        } catch (e: Exception) {
            _state.value = ImportResultState.Error("Invalid JSON structure: ${e.localizedMessage}", e)
            return
        }

        val validation = ValidationResult.validateStructure(backupData)
        if (validation is ValidationResult.Invalid) {
            _state.value = ImportResultState.Error("Validation failed:\n${validation.errors.joinToString("\n")}")
            return
        }

        _state.value = ImportResultState.AwaitingMode(backupData)
    }

    fun selectMode(mode: ImportMode) {
        val currentState = _state.value
        val backupData = (currentState as? ImportResultState.AwaitingMode)?.backupData ?: return

        viewModelScope.launch {
            _state.value = ImportResultState.BuildingPlan(mode)

            if (mode == ImportMode.RESTORE) {
                val restoreValidation = ValidationResult.validateForRestore(backupData)
                if (restoreValidation is ValidationResult.Invalid) {
                    _state.value = ImportResultState.Error("Restore validation failed:\n${restoreValidation.errors.joinToString("\n")}")
                    return@launch
                }
                val emptyPlan = ImportPlan()
                _state.value = ImportResultState.Preview(emptyPlan, mode, backupData)
            } else {
                val localSnapshot = settingsRepository.createLocalSnapshot()
                val plan = MergePlanBuilder.buildMergePlan(backupData, localSnapshot)
                _state.value = ImportResultState.Preview(plan, mode, backupData)
            }
        }
    }

    fun proceedFromPreview() {
        val currentState = _state.value as? ImportResultState.Preview ?: return
        if (currentState.mode == ImportMode.RESTORE) {
            applyRestore(currentState.backupData)
        } else {
            if (currentState.plan.conflicts.isNotEmpty()) {
                val initialResolutions = currentState.plan.conflicts.associate { it.backupItemId to ConflictResolution.KEEP_LOCAL }
                _state.value = ImportResultState.ResolvingConflicts(
                    plan = currentState.plan,
                    mode = currentState.mode,
                    backupData = currentState.backupData,
                    resolutions = initialResolutions,
                )
            } else {
                applyMerge(currentState.plan, emptyMap(), emptyMap())
            }
        }
    }

    fun setResolution(conflictId: String, resolution: ConflictResolution) {
        val currentState = _state.value as? ImportResultState.ResolvingConflicts ?: return
        val updatedMap = currentState.resolutions.toMutableMap()
        updatedMap[conflictId] = resolution
        _state.value = currentState.copy(resolutions = updatedMap)
    }

    fun setFieldSelection(conflictId: String, selection: FieldMergeSelection) {
        val currentState = _state.value as? ImportResultState.ResolvingConflicts ?: return
        val updatedSelections = currentState.fieldSelections.toMutableMap()
        updatedSelections[conflictId] = selection
        val updatedResolutions = currentState.resolutions.toMutableMap()
        updatedResolutions[conflictId] = ConflictResolution.MERGE_FIELDS
        _state.value = currentState.copy(resolutions = updatedResolutions, fieldSelections = updatedSelections)
    }

    fun setBulkResolution(resolution: ConflictResolution) {
        val currentState = _state.value as? ImportResultState.ResolvingConflicts ?: return
        val updatedMap = currentState.plan.conflicts.associate { conflict ->
            val defaultRes = when (conflict) {
                is ImportConflict.GameMetadata -> resolution
                is ImportConflict.GameRawgId -> if (resolution == ConflictResolution.KEEP_BACKUP) ConflictResolution.USE_BACKUP else ConflictResolution.USE_LOCAL
                is ImportConflict.ListName -> if (resolution == ConflictResolution.KEEP_BACKUP) ConflictResolution.KEEP_BACKUP else ConflictResolution.KEEP_LOCAL
                is ImportConflict.Membership -> if (resolution == ConflictResolution.KEEP_BACKUP) ConflictResolution.KEEP_BACKUP else ConflictResolution.KEEP_LOCAL
            }
            conflict.backupItemId to defaultRes
        }
        _state.value = currentState.copy(resolutions = updatedMap)
    }

    fun applyResolvedMerge() {
        val currentState = _state.value as? ImportResultState.ResolvingConflicts ?: return
        applyMerge(currentState.plan, currentState.resolutions, currentState.fieldSelections)
    }

    private fun applyMerge(plan: ImportPlan, resolutions: Map<String, ConflictResolution>, fieldSelections: Map<String, FieldMergeSelection>) {
        val mode = ImportMode.MERGE
        _state.value = ImportResultState.Applying(mode)
        viewModelScope.launch {
            val result = settingsRepository.applyMergePlan(plan, resolutions, fieldSelections)
            result.fold(
                onSuccess = { report ->
                    _state.value = ImportResultState.Done(report)
                },
                onFailure = { error ->
                    _state.value = ImportResultState.Error("Import failed: ${error.localizedMessage}", error)
                },
            )
        }
    }

    private fun applyRestore(backupData: ExportData) {
        val mode = ImportMode.RESTORE
        _state.value = ImportResultState.Applying(mode)
        viewModelScope.launch {
            val result = settingsRepository.applyRestorePlan(backupData)
            result.fold(
                onSuccess = { report ->
                    _state.value = ImportResultState.Done(report)
                },
                onFailure = { error ->
                    _state.value = ImportResultState.Error("Restore failed: ${error.localizedMessage}", error)
                },
            )
        }
    }
}
