package com.backlogorder.domain.repository

import android.net.Uri
import com.backlogorder.domain.ExportData
import com.backlogorder.domain.model.ThemeId
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val onboardingComplete: Flow<Boolean>
    suspend fun setOnboardingComplete(value: Boolean)

    val theme: Flow<ThemeId>
    suspend fun setTheme(themeId: ThemeId)
    
    val isDarkMode: Flow<Boolean>
    suspend fun setDarkMode(value: Boolean)

    val customColorHex: Flow<String>
    suspend fun setCustomColorHex(hex: String)

    // Export functionality
    suspend fun exportData(): Result<ExportData>
    suspend fun shareExportedData(exportedData: ExportData): Result<Uri>
    
    // Import functionality
    suspend fun importData(jsonString: String): Result<Unit>
    suspend fun openDocumentForImport(): Result<String>
    
    // Schema validation
    fun validateExportSchema(): Result<Unit>
}
