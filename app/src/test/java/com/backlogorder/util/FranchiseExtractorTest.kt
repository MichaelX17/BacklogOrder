package com.backlogorder.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FranchiseExtractorTest {

    @Test
    fun `extractFranchiseName strips subtitles after colon or dash`() {
        assertEquals("The Witcher", FranchiseExtractor.extractFranchiseName("The Witcher 3: Wild Hunt"))
        assertEquals("The Legend of Zelda", FranchiseExtractor.extractFranchiseName("The Legend of Zelda: Tears of the Kingdom"))
        assertEquals("Call of Duty", FranchiseExtractor.extractFranchiseName("Call of Duty: Modern Warfare"))
    }

    @Test
    fun `extractFranchiseName strips numbers and roman numerals`() {
        assertEquals("The Witcher", FranchiseExtractor.extractFranchiseName("The Witcher 3"))
        assertEquals("Final Fantasy", FranchiseExtractor.extractFranchiseName("Final Fantasy VII"))
        assertEquals("Grand Theft Auto", FranchiseExtractor.extractFranchiseName("Grand Theft Auto V"))
        assertEquals("Dark Souls", FranchiseExtractor.extractFranchiseName("Dark Souls III"))
        assertEquals("Persona", FranchiseExtractor.extractFranchiseName("Persona 5"))
        assertEquals("Fallout", FranchiseExtractor.extractFranchiseName("Fallout 4"))
    }

    @Test
    fun `extractFranchiseName cleans up edition keywords and subtitles`() {
        assertEquals("Persona", FranchiseExtractor.extractFranchiseName("Persona 5 Royal"))
        assertEquals("Final Fantasy", FranchiseExtractor.extractFranchiseName("Final Fantasy VII Remake"))
        assertEquals("God of War", FranchiseExtractor.extractFranchiseName("God of War Ragnarök"))
        assertEquals("Cyberpunk", FranchiseExtractor.extractFranchiseName("Cyberpunk 2077"))
    }

    @Test
    fun `extractFranchiseName preserves standalone title when no franchise pattern present`() {
        assertEquals("Hollow Knight", FranchiseExtractor.extractFranchiseName("Hollow Knight"))
        assertEquals("Elden Ring", FranchiseExtractor.extractFranchiseName("Elden Ring"))
    }

    @Test
    fun `extractFranchiseName uses explicit tag if available`() {
        assertEquals("Witcher Series", FranchiseExtractor.extractFranchiseName("Wild Hunt", listOf("franchise: Witcher Series")))
    }
}
