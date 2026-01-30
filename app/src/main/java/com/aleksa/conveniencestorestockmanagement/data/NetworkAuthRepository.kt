package com.aleksa.conveniencestorestockmanagement.data

import com.aleksa.conveniencestorestockmanagement.domain.AuthRepository
import com.aleksa.data.remote.AuthLoginRequest
import com.aleksa.data.remote.AuthLoginResponse
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.execute
import com.aleksa.network.api.ApiPaths
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class NetworkAuthRepository @Inject constructor(
    private val executor: NetworkExecutor,
) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String,
    ): NetworkResult<Boolean, ErrorResponse> {
        val result = executor.execute<AuthLoginResponse> {
            post(ApiPaths.AUTH_LOGIN) {
                contentType(ContentType.Application.Json)
                setBody(AuthLoginRequest(username = username, password = password))
            }
        }
        return when (result) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.authenticated)
            is NetworkResult.Error -> NetworkResult.Error(result.error)
        }
    }
}
