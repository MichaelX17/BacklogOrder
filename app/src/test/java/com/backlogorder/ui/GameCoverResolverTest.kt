package com.backlogorder.ui

import com.backlogorder.ui.common.resolveCoverModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class GameCoverResolverTest {

    @Test
    fun resolveCoverModel_returnsLocalPathWhenFileExists(): Unit = runBlocking {
        val tempFile = File.createTempFile("test_cover", ".jpg")
        tempFile.writeText("sample data")

        val result = resolveCoverModel(
            localPath = tempFile.absolutePath,
            url = "https://example.com/cover.jpg",
            gameId = "game-1",
            refresher = null,
        )

        assertEquals(tempFile.absolutePath, result)
        tempFile.delete()
    }

    @Test
    fun resolveCoverModel_returnsUrlWhenLocalFileMissing(): Unit = runBlocking {
        val result = resolveCoverModel(
            localPath = "/non/existent/path/cover.jpg",
            url = "https://example.com/cover.jpg",
            gameId = "game-1",
            refresher = null,
        )

        assertEquals("https://example.com/cover.jpg", result)
    }

    @Test
    fun resolveCoverModel_returnsUrlWhenLocalPathNull(): Unit = runBlocking {
        val result = resolveCoverModel(
            localPath = null,
            url = "https://example.com/cover.jpg",
            gameId = "game-1",
            refresher = null,
        )

        assertEquals("https://example.com/cover.jpg", result)
    }

    @Test
    fun resolveCoverModel_returnsNullWhenBothNull(): Unit = runBlocking {
        val result = resolveCoverModel(
            localPath = null,
            url = null,
            gameId = "game-1",
            refresher = null,
        )

        assertNull(result)
    }

    @Test
    fun resolveCoverModel_returnsContentUri(): Unit = runBlocking {
        val result = resolveCoverModel(
            localPath = "content://media/external/images/media/100",
            url = null,
            gameId = "game-1",
            refresher = null,
        )

        assertEquals("content://media/external/images/media/100", result)
    }

    @Test
    fun resolveCoverModel_returnsFileUriWhenFileExists(): Unit = runBlocking {
        val tempFile = File.createTempFile("test_cover_file_uri", ".jpg")
        val fileUri = "file://${tempFile.absolutePath}"

        val result = resolveCoverModel(
            localPath = fileUri,
            url = "https://example.com/cover.jpg",
            gameId = "game-1",
            refresher = null,
        )

        assertEquals(fileUri, result)
        tempFile.delete()
    }
}
