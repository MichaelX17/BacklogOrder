package com.backlogorder.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.backlogorder.domain.repository.ApiKeyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncryptedApiKeyProvider(
    context: Context,
) : ApiKeyProvider {
    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "backlogorder_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val keyFlow = MutableStateFlow(prefs.getString("api_key", null))

    override val key: Flow<String?> = keyFlow.asStateFlow()

    override val currentKey: String? get() = keyFlow.value

    override suspend fun setKey(value: String) {
        prefs.edit().putString("api_key", value).apply()
        keyFlow.value = value
    }

    override suspend fun clear() {
        prefs.edit().remove("api_key").apply()
        keyFlow.value = null
    }
}
