package com.aleksa.domain

import com.aleksa.domain.model.Supplier
import kotlinx.coroutines.flow.Flow

interface SupplierRepository {
    fun observeAll(): Flow<List<Supplier>>
    fun observeSearch(query: String): Flow<List<Supplier>>
    suspend fun upsert(supplier: Supplier)
}
