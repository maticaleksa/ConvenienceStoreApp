package com.aleksa.domain

import com.aleksa.domain.model.Supplier
import kotlinx.coroutines.flow.Flow

/**
 * Access to supplier data sources and persistence operations.
 */
interface SupplierRepository {
    /**
     * Observes all suppliers.
     */
    fun observeAll(): Flow<List<Supplier>>

    /**
     * Observes suppliers matching the given search query.
     *
     * Search is case-insensitive and uses partial matches. Implementations are expected
     * to match against supplier name, contact person, phone, email, and address. Blank
     * queries should behave the same as [observeAll].
     */
    fun observeSearch(query: String): Flow<List<Supplier>>

    /**
     * Inserts or updates the provided supplier.
     */
    suspend fun upsert(supplier: Supplier)
}
