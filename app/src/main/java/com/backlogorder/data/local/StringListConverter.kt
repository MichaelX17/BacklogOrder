package com.backlogorder.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class StringListConverter {
    private val json = Json

    @TypeConverter
    fun fromString(value: String): List<String> = if (value.isBlank()) {
        emptyList()
    } else {
        runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun toString(value: List<String>): String = json.encodeToString(value)
}
