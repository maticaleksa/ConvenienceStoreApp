package com.aleksa.data.source

import com.aleksa.data.remote.SupplierDto
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkResult

interface SupplierRemoteDataSource {
    suspend fun fetchAll(): NetworkResult<List<SupplierDto>, ErrorResponse>
    suspend fun upsert(supplier: SupplierDto): NetworkResult<SupplierDto, ErrorResponse>
}
