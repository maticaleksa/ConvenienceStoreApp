package com.aleksa.data.source

import com.aleksa.data.database.SupplierEntity
import kotlinx.coroutines.flow.Flow

/**
 * Local supplier persistence and queries.
 */
interface SupplierDataSource {
    fun getAllFlow(): Flow<List<SupplierEntity>>

    /**
     * Searches across common supplier fields using a case-insensitive LIKE query.
     *
     * Pass a wildcarded query string (for example "%term%").
     */
    fun searchFlow(query: String): Flow<List<SupplierEntity>>
    suspend fun getAll(): List<SupplierEntity>
    suspend fun getAllIds(): List<String>
    suspend fun upsertAll(suppliers: List<SupplierEntity>)
    suspend fun upsert(supplier: SupplierEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
