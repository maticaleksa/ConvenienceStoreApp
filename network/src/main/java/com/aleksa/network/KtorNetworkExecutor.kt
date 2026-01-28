package com.aleksa.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class KtorNetworkExecutor(
    private val client: HttpClient,
) : NetworkExecutor {
    override suspend fun executeRaw(
        block: suspend HttpClient.() -> HttpResponse,
    ): NetworkResult<HttpResponse, ErrorResponse> {
        return try {
            val response = block(client)
            if (response.status.isSuccess()) {
                NetworkResult.Success(response)
            } else {
                val error = runCatching { response.body<ErrorResponse>() }.getOrNull()
                    ?: ErrorResponse(message = "HTTP ${response.status.value}")
                NetworkResult.Error(error)
            }
        } catch (e: Exception) {
            NetworkResult.Error(ErrorResponse(message = e.message ?: "Network request failed"))
        }
    }

}
