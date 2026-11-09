package com.backlogorder.domain.repository

import kotlinx.coroutines.flow.Flow

interface ApiKeyProvider {
    val key: Flow<String?>
    val currentKey: String? get() = null
    suspend fun setKey(value: String)
    suspend fun clear()
}
