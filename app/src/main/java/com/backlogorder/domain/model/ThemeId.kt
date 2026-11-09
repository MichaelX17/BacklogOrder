package com.backlogorder.domain.model

enum class ThemeId {
    VIOLET,
    EMERALD,
    CRIMSON,
    CUSTOM;

    fun storageValue(): String = name.lowercase()

    companion object {
        fun fromStorage(value: String?): ThemeId = when (value?.trim()?.lowercase()) {
            "emerald" -> EMERALD
            "crimson" -> CRIMSON
            "custom" -> CUSTOM
            else -> VIOLET
        }
    }
}
