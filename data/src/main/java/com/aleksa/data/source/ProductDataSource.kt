package com.aleksa.data.source

import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.ProductWithCategorySupplier
import kotlinx.coroutines.flow.Flow

interface ProductDataSource {
    suspend fun getAll(): List<ProductWithCategorySupplier>
    fun getAllFlow(): Flow<List<ProductWithCategorySupplier>>
    fun searchFlow(query: String): Flow<List<ProductWithCategorySupplier>>
    suspend fun count(): Int
    suspend fun getAllIds(): List<String>
    suspend fun getById(id: String): ProductWithCategorySupplier?
    fun getByIdFlow(id: String): Flow<ProductWithCategorySupplier?>
    suspend fun upsertAll(products: List<ProductEntity>)
    suspend fun clearAll()
    suspend fun upsert(product: ProductEntity)
    suspend fun upsertAndGet(product: ProductEntity): ProductWithCategorySupplier?
    suspend fun delete(product: ProductEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
