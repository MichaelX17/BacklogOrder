package com.backlogorder.domain

import com.backlogorder.util.CsvParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvParserTest {
    @Test
    fun `parseHandlesQuotedFieldsAndLineBreaks`() {
        val csv = "\"Name, Inc.\",\"City, State\",42\r\n\"Alpha\",\"Beta\"\n"

        val rows = CsvParser.parse(csv)

        assertEquals(listOf(listOf("Name, Inc.", "City, State", "42"), listOf("Alpha", "Beta")), rows)
    }
}
