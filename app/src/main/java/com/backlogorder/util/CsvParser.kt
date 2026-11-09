package com.backlogorder.util

object CsvParser {
    fun parse(text: String): List<List<String>> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val rows = mutableListOf<MutableList<String>>()
        val currentRow = mutableListOf<String>()
        val currentCell = StringBuilder()
        var inQuotes = false
        var rowCompleted = false
        var i = 0

        while (i < text.length) {
            val char = text[i]
            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < text.length && text[i + 1] == '"') {
                        currentCell.append('"')
                        i += 1
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                char == ',' && !inQuotes -> {
                    currentRow.add(currentCell.toString().trim())
                    currentCell.clear()
                    rowCompleted = false
                }

                char == '\n' && !inQuotes -> {
                    currentRow.add(currentCell.toString().trim())
                    rows.add(currentRow.toMutableList())
                    currentRow.clear()
                    currentCell.clear()
                    rowCompleted = true
                    i += 1
                    continue
                }

                char == '\r' && !inQuotes -> {
                    currentRow.add(currentCell.toString().trim())
                    rows.add(currentRow.toMutableList())
                    currentRow.clear()
                    currentCell.clear()
                    rowCompleted = true
                    if (i + 1 < text.length && text[i + 1] == '\n') {
                        i += 1
                    }
                }

                else -> currentCell.append(char)
            }
            i += 1
        }

        if (!rowCompleted && (currentCell.isNotEmpty() || currentRow.isNotEmpty())) {
            currentRow.add(currentCell.toString().trim())
            rows.add(currentRow)
        }

        return rows
    }
}
