package com.backlogorder.domain.error

sealed class AppError(override val message: String) : Exception(message) {
    data class Network(override val message: String = "Network error") : AppError(message)
    data class Unauthorized(override val message: String = "Unauthorized") : AppError(message)
    data class RateLimited(override val message: String = "Rate limited") : AppError(message)
    data class NotFound(override val message: String = "Not found") : AppError(message)
    data class Timeout(override val message: String = "Request timed out") : AppError(message)
    data class Validation(override val message: String) : AppError(message)
    data class Unknown(override val message: String = "Unknown error") : AppError(message)
}
