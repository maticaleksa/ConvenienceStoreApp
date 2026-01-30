package com.aleksa.network.fake

import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class FakeRequest(
    val method: String,
    val path: String,
    val bodyText: String?,
)

class FakeNetworkExecutor(
    private val routes: Map<String, NetworkResult<Any, ErrorResponse>>,
    private val handlers: Map<String, (FakeRequest) -> NetworkResult<Any, ErrorResponse>> =
        emptyMap(),
    private val isNetworkAvailable: () -> Boolean = { true },
) : NetworkExecutor {

    private val mockClient = HttpClient(MockEngine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }

        engine {
            addHandler { request ->
                if (!isNetworkAvailable()) {
                    return@addHandler respond(
                        content = Json.encodeToString(
                            ErrorResponse.serializer(),
                            ErrorResponse(message = "No network connection"),
                        ),
                        status = HttpStatusCode.ServiceUnavailable,
                        headers = headersOf(
                            "Content-Type" to listOf(ContentType.Application.Json.toString()),
                        ),
                    )
                }
                val path = request.url.encodedPath
                val method = request.method.value
                val key = "$method $path"
                val handler = handlers[key] ?: handlers[path]
                val mapped = if (handler != null) {
                    handler(
                        FakeRequest(
                            method = method,
                            path = path,
                            bodyText = readBodyText(request.body),
                        ),
                    )
                } else {
                    routes[key] ?: routes[path]
                }
                    ?: return@addHandler respond(
                        content = """{"message":"No fake response registered for path: $path"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                    )

                when (mapped) {
                    is NetworkResult.Success -> {
                        val dto = mapped.data
                        val content = when (dto) {
                            is String -> dto
                            is JsonElement -> dto.toString()
                            else -> Json.encodeToString(AnyAsJsonSerializer, dto)
                        }
                        respond(
                            content = content,
                            status = HttpStatusCode.OK,
                            headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                        )
                    }
                    is NetworkResult.Error -> {
                        respond(
                            content = Json.encodeToString(ErrorResponse.serializer(), mapped.error),
                            status = HttpStatusCode.BadRequest,
                            headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                        )
                    }
                    else -> {
                        respond(
                            content = """{"message":"Invalid fake response"}""",
                            status = HttpStatusCode.InternalServerError,
                            headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                        )
                    }
                }
            }
        }
    }

    override suspend fun executeRaw(
        block: suspend HttpClient.() -> HttpResponse,
    ): NetworkResult<HttpResponse, ErrorResponse> {
        return try {
            val response = block(mockClient)
            NetworkResult.Success(response)
        } catch (e: Exception) {
            NetworkResult.Error(ErrorResponse(message = e.message ?: "Network request failed"))
        }
    }
}

private suspend fun readBodyText(body: OutgoingContent): String? {
    return when (body) {
        is OutgoingContent.ByteArrayContent -> body.bytes().decodeToString()
        is TextContent -> body.text
        is OutgoingContent.ReadChannelContent -> body.readFrom().readRemaining().readText()
        else -> null
    }
}

internal object AnyAsJsonSerializer : KSerializer<Any> {
    override val descriptor = buildClassSerialDescriptor("Any")

    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("AnyAsJsonSerializer requires JsonEncoder")
        jsonEncoder.encodeJsonElement(guessJsonElement(value))
    }

    override fun deserialize(decoder: Decoder): Any {
        error("Not needed")
    }
}

private fun guessJsonElement(value: Any): JsonElement {
    return when (value) {
        is JsonElement -> value
        is String -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Map<*, *> -> JsonObject(
            value.entries
                .filter { it.key is String }
                .associate { it.key as String to guessJsonElement(it.value ?: JsonNull) },
        )
        else -> JsonPrimitive(value.toString())
    }
}
