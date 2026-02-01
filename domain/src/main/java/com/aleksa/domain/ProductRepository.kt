package com.aleksa.domain

import com.aleksa.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Access to product data sources and persistence operations.
 */
interface ProductRepository {
    /**
     * Observes all products.
     */
    fun observeAll(): Flow<List<Product>>

    /**
     * Observes products matching the given search query.
     *
     * Search is case-insensitive and uses partial matches. Implementations are expected
     * to match against product name, barcode, and category name. Blank queries should
     * behave the same as [observeAll].
     */
    fun observeSearch(query: String): Flow<List<Product>>

    /**
     * Inserts or updates the provided product.
     */
    suspend fun upsert(product: Product)
}
