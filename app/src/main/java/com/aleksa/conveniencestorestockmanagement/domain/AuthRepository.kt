package com.aleksa.conveniencestorestockmanagement.domain

import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkResult

interface AuthRepository {
    suspend fun login(username: String, password: String): NetworkResult<Boolean, ErrorResponse>
}
