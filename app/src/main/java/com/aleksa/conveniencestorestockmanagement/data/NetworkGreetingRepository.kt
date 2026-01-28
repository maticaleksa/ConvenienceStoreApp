package com.aleksa.conveniencestorestockmanagement.data

import com.aleksa.conveniencestorestockmanagement.domain.GreetingRepository
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.execute
import io.ktor.client.request.get
import javax.inject.Inject

class NetworkGreetingRepository @Inject constructor(
    private val executor: NetworkExecutor,
) : GreetingRepository {
    override suspend fun fetchGreeting(): String {
        val result = executor.execute<String> { get("/greeting") }
        return when (result) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> result.error.message ?: "Error loading greeting"
        }
    }
}
