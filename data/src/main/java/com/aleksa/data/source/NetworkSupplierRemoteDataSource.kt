package com.aleksa.data.source

import com.aleksa.data.remote.SupplierDto
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.api.ApiPaths
import com.aleksa.network.execute
import com.aleksa.data.remote.jsonBody
import io.ktor.client.request.get
import io.ktor.client.request.post
import javax.inject.Inject

class NetworkSupplierRemoteDataSource @Inject constructor(
    private val networkExecutor: NetworkExecutor,
) : SupplierRemoteDataSource {
    override suspend fun fetchAll(): NetworkResult<List<SupplierDto>, ErrorResponse> {
        return networkExecutor.execute { get(ApiPaths.SUPPLIERS) }
    }

    override suspend fun upsert(supplier: SupplierDto): NetworkResult<SupplierDto, ErrorResponse> {
        return networkExecutor.execute {
            post(ApiPaths.SUPPLIERS) {
                jsonBody(supplier)
            }
        }
    }
}
