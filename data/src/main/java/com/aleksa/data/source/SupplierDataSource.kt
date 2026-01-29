package com.aleksa.data.source

import com.aleksa.data.database.SupplierEntity
import kotlinx.coroutines.flow.Flow

interface SupplierDataSource {
    fun getAllFlow(): Flow<List<SupplierEntity>>
    fun searchFlow(query: String): Flow<List<SupplierEntity>>
    suspend fun getAll(): List<SupplierEntity>
    suspend fun getAllIds(): List<String>
    suspend fun upsertAll(suppliers: List<SupplierEntity>)
    suspend fun upsert(supplier: SupplierEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
