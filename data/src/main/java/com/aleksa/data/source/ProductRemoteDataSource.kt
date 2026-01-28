package com.aleksa.data.source

import com.aleksa.data.remote.ProductDto
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkResult

interface ProductRemoteDataSource {
    suspend fun fetchAll(): NetworkResult<List<ProductDto>, ErrorResponse>
}
