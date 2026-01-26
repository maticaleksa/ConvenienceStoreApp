package com.aleksa.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorNetworkExecutor(
    private val client: HttpClient = createDefaultClient(),
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
            NetworkResult.Exception(e)
        }
    }

    companion object {
        fun createDefaultClient(): HttpClient {
            return HttpClient {
                expectSuccess = false
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }
        }
    }
}

private fun HttpStatusCode.isSuccess(): Boolean = value in 200..299
