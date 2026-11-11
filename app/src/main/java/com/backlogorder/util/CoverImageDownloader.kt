package com.backlogorder.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

open class CoverImageDownloader(private val context: Context) {

    open suspend fun download(gameId: String, imageUrl: String?): Result<String> = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Image URL is null or blank"))
        }
        if (!imageUrl.startsWith("http://", ignoreCase = true) && !imageUrl.startsWith("https://", ignoreCase = true)) {
            // Already a local file path
            return@withContext Result.success(imageUrl)
        }

        try {
            val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
            val fileName = "cover_${gameId.lowercase(Locale.ROOT)}.jpg"
            val targetFile = File(coversDir, fileName)

            if (targetFile.exists() && targetFile.length() > 0) {
                return@withContext Result.success(targetFile.absolutePath)
            }

            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (targetFile.exists() && targetFile.length() > 0) {
                    return@withContext Result.success(targetFile.absolutePath)
                }
            }
            Result.failure(IOException("Failed HTTP download: response code ${connection.responseCode}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAndSaveCover(imageUrl: String?, gameId: String): String? {
        val res = download(gameId, imageUrl)
        return res.getOrNull()
    }
}
