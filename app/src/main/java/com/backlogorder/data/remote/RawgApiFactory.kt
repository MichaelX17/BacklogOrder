package com.backlogorder.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit

object RawgApiFactory {
    fun create(
        baseUrl: String,
        apiKeyProvider: () -> String? = { null },
    ): RawgApiService {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(RawgApiKeyInterceptor(apiKeyProvider))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RawgApiService::class.java)
    }
}

class RawgApiKeyInterceptor(
    private val apiKeyProvider: () -> String? = { null },
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = apiKeyProvider()
        val requestBuilder = chain.request().newBuilder()
            .addHeader("User-Agent", "BacklogOrder/1.0")
        if (!apiKey.isNullOrBlank()) {
            requestBuilder.addHeader("X-API-Key", apiKey)
        }
        return chain.proceed(requestBuilder.build())
    }
}
