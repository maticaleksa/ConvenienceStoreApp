package com.aleksa.data.source

import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.ProductWithCategorySupplier
import kotlinx.coroutines.flow.Flow

/**
 * Local product persistence and queries.
 */
interface ProductDataSource {
    suspend fun getAll(): List<ProductWithCategorySupplier>
    fun getAllFlow(): Flow<List<ProductWithCategorySupplier>>

    /**
     * Searches by name, barcode, or category name using a case-insensitive LIKE query.
     *
     * Pass a wildcarded query string (for example "%term%").
     */
    fun searchFlow(query: String): Flow<List<ProductWithCategorySupplier>>
    suspend fun count(): Int
    suspend fun getAllIds(): List<String>

    /**
     * Returns supplier ids referenced by any stored product.
     */
    suspend fun getSupplierIdsInUse(): List<String>
    suspend fun getById(id: String): ProductWithCategorySupplier?
    fun getByIdFlow(id: String): Flow<ProductWithCategorySupplier?>
    suspend fun upsertAll(products: List<ProductEntity>)
    suspend fun clearAll()
    suspend fun upsert(product: ProductEntity)
    suspend fun upsertAndGet(product: ProductEntity): ProductWithCategorySupplier?
    suspend fun delete(product: ProductEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
