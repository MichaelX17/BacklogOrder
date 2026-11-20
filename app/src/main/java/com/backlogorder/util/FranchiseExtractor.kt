package com.backlogorder.util

object FranchiseExtractor {

    private val KEYWORD_CLEANUP_REGEX = Regex(
        "(?i)\\b(remastered|remake|definitive|goty|game of the year|special|collector'?s|ultimate|enhanced|deluxe|anniversary|director'?s cut|royal|complete|legendary|re-mastered|hd|edition|version|bundle|collection)\\b"
    )

    private val NUMBER_OR_ROMAN_TRAILING_REGEX = Regex(
        "(?i)\\s+(?:(?:19|20)\\d\\d|[0-9]{1,2}|XVI|XV|XIV|XIII|XII|XI|X|IX|VIII|VII|VI|V|IV|III|II|I)\\b"
    )

    private val SUBTITLE_TRAILING_WORDS = setOf(
        "ragnarok", "ragnarök", "valhalla", "village", "infinite", "strikers", "unbound",
        "odyssey", "origins", "legion", "rebirth", "intergrade", "breakpoint", "wildlands",
        "syndicate", "unity", "vanguard", "wilds", "rise", "zero", "returns", "forever",
        "revelation", "revelations", "brotherhood", "subzero", "tribes", "tactics", "unleashed"
    )

    fun extractFranchiseName(gameName: String, tags: List<String> = emptyList()): String {
        val trimmed = gameName.trim()
        if (trimmed.isBlank()) return "Standalone Game"

        // 1. Check if tags contain explicit franchise info (e.g., "franchise: the-witcher")
        for (tag in tags) {
            val lower = tag.trim().lowercase()
            if (lower.startsWith("franchise:") || lower.startsWith("series:")) {
                val extracted = tag.substringAfter(":").trim()
                if (extracted.isNotBlank()) return extracted
            }
        }

        // 2. Process main title before colon or dash
        var result = trimmed
        if (result.contains(":") || result.contains(" - ") || result.contains(" – ") || result.contains(" — ")) {
            val parts = result.split(Regex("[:\\-–—]"))
            if (parts.isNotEmpty() && parts[0].trim().isNotBlank()) {
                result = parts[0].trim()
            }
        }

        // 3. Remove edition / version / remaster words
        result = KEYWORD_CLEANUP_REGEX.replace(result, "").trim()

        // 4. Remove trailing numbers or Roman numerals (e.g., "The Witcher 3" -> "The Witcher")
        val strippedNumber = NUMBER_OR_ROMAN_TRAILING_REGEX.replace(result, "").trim()
        if (strippedNumber.isNotBlank()) {
            result = strippedNumber
        }

        // 5. If trailing word is a known subtitle word (e.g. "God of War Ragnarök" -> "God of War")
        val words = result.split("\\s+".toRegex())
        if (words.size > 1) {
            val lastWord = words.last().lowercase()
            if (SUBTITLE_TRAILING_WORDS.contains(lastWord)) {
                val candidate = words.dropLast(1).joinToString(" ").trim()
                if (candidate.isNotBlank()) {
                    result = candidate
                }
            }
        }

        // 6. Clean up trailing/leading punctuation
        result = result.trimEnd(',', '.', ':', '-', ' ', '–', '—').trim()

        return result.ifBlank { trimmed }
    }
}
