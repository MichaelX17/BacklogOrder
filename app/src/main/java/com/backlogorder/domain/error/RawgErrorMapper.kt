package com.backlogorder.domain.error

import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

class RawgErrorMapper {
    fun map(statusCode: Int? = null, throwable: Throwable? = null): AppError {
        val detail = throwable?.message ?: throwable?.localizedMessage
        return when {
            (statusCode == 401 || statusCode == 403) -> AppError.Unauthorized(
                if (!detail.isNullOrBlank()) "Invalid or unauthorized RAWG API key ($detail)" else "Invalid or unauthorized RAWG API key",
            )
            statusCode == 404 -> AppError.NotFound()
            statusCode == 429 -> AppError.RateLimited()
            throwable is TimeoutException || throwable is SocketTimeoutException -> AppError.Timeout(
                "Request timed out while reaching RAWG API. Please try again.",
            )
            throwable is IOException -> AppError.Network(
                if (!detail.isNullOrBlank()) "Network error: $detail" else "Network error",
            )
            else -> AppError.Unknown(
                if (!detail.isNullOrBlank()) "Error: $detail" else "Unknown error",
            )
        }
    }
}
