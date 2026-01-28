package com.aleksa.network

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

sealed class NetworkResult<out T, out E> {
    data class Success<T>(val data: T) : NetworkResult<T, Nothing>()
    data class Error<E>(val error: E) : NetworkResult<Nothing, E>()
}

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val code: String? = null,
    val details: Map<String, String>? = null,
)

interface NetworkExecutor {
    suspend fun executeRaw(
        block: suspend HttpClient.() -> HttpResponse,
    ): NetworkResult<HttpResponse, ErrorResponse>
}
