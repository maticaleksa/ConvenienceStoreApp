package com.aleksa.data.source

import com.aleksa.data.remote.TransactionDto
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkResult

interface TransactionRemoteDataSource {
    suspend fun fetchAll(): NetworkResult<List<TransactionDto>, ErrorResponse>
    suspend fun upsert(transaction: TransactionDto): NetworkResult<TransactionDto, ErrorResponse>
}
