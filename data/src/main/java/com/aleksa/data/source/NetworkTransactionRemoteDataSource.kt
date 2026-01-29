package com.aleksa.data.source

import com.aleksa.data.remote.TransactionDto
import com.aleksa.data.remote.jsonBody
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.api.ApiPaths
import com.aleksa.network.execute
import io.ktor.client.request.get
import io.ktor.client.request.post
import javax.inject.Inject

class NetworkTransactionRemoteDataSource @Inject constructor(
    private val networkExecutor: NetworkExecutor,
) : TransactionRemoteDataSource {
    override suspend fun fetchAll(): NetworkResult<List<TransactionDto>, ErrorResponse> {
        return networkExecutor.execute { get(ApiPaths.TRANSACTIONS) }
    }

    override suspend fun upsert(transaction: TransactionDto): NetworkResult<TransactionDto, ErrorResponse> {
        return networkExecutor.execute {
            post(ApiPaths.TRANSACTIONS) { jsonBody(transaction) }
        }
    }
}
