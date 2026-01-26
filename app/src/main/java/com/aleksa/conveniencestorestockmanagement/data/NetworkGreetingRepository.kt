package com.aleksa.conveniencestorestockmanagement.data

import com.aleksa.conveniencestorestockmanagement.domain.GreetingRepository
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class NetworkGreetingRepository @Inject constructor(
    private val executor: NetworkExecutor,
) : GreetingRepository {
    override suspend fun fetchGreeting(): String {
        val result = executor.executeRaw {
            get("https://fake.local/greeting")
        }
        return when (result) {
            is NetworkResult.Success -> result.data.body()
            is NetworkResult.Error -> result.error.message ?: "Error loading greeting"
            is NetworkResult.Exception -> "Network exception"
        }
    }
}
