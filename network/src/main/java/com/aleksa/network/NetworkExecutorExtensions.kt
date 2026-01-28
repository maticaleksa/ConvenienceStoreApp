package com.aleksa.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

suspend inline fun <reified T> NetworkExecutor.execute(
    noinline block: suspend HttpClient.() -> HttpResponse,
): NetworkResult<T, ErrorResponse> {
    return when (val rawResult = executeRaw(block)) {
        is NetworkResult.Success -> mapResponse(rawResult.data)
        is NetworkResult.Error -> NetworkResult.Error(rawResult.error)
    }
}

suspend inline fun <reified T> mapResponse(response: HttpResponse): NetworkResult<T, ErrorResponse> {
    return if (response.status.isSuccess()) {
        NetworkResult.Success(response.body())
    } else {
        val error = runCatching { response.body<ErrorResponse>() }
            .getOrNull()
            ?: ErrorResponse(message = "HTTP ${response.status.value}")
        NetworkResult.Error(error)
    }
}

fun HttpStatusCode.isSuccess(): Boolean = value in 200..299
