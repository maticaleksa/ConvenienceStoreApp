package com.aleksa.data.source

import com.aleksa.data.remote.ProductDto
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.api.ApiPaths
import com.aleksa.network.execute
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class NetworkProductRemoteDataSource @Inject constructor(
    private val networkExecutor: NetworkExecutor,
) : ProductRemoteDataSource {
    override suspend fun fetchAll(): NetworkResult<List<ProductDto>, ErrorResponse> {
        return networkExecutor.execute { get(ApiPaths.PRODUCTS) }
    }

    override suspend fun upsert(product: ProductDto): NetworkResult<ProductDto, ErrorResponse> {
        return networkExecutor.execute {
            post(ApiPaths.PRODUCTS) {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
        }
    }
}
