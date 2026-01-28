package com.aleksa.data.source

import com.aleksa.data.database.ProductEntity
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
    suspend fun getAll(): List<ProductEntity>
    fun getAllFlow(): Flow<List<ProductEntity>>
    fun searchFlow(query: String): Flow<List<ProductEntity>>
    suspend fun count(): Int
    suspend fun getAllIds(): List<String>
    suspend fun getById(id: String): ProductEntity?
    fun getByIdFlow(id: String): Flow<ProductEntity?>
    suspend fun upsertAll(products: List<ProductEntity>)
    suspend fun clearAll()
    suspend fun upsert(product: ProductEntity)
    suspend fun delete(product: ProductEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
