package com.aleksa.data.source

import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.ProductWithCategory
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
    suspend fun getAll(): List<ProductWithCategory>
    fun getAllFlow(): Flow<List<ProductWithCategory>>
    fun searchFlow(query: String): Flow<List<ProductWithCategory>>
    suspend fun count(): Int
    suspend fun getAllIds(): List<String>
    suspend fun getById(id: String): ProductWithCategory?
    fun getByIdFlow(id: String): Flow<ProductWithCategory?>
    suspend fun upsertAll(products: List<ProductEntity>)
    suspend fun clearAll()
    suspend fun upsert(product: ProductEntity)
    suspend fun delete(product: ProductEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
