package com.aleksa.domain

import com.aleksa.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    fun observeSearch(query: String): Flow<List<Product>>
    suspend fun upsert(product: Product)
}
