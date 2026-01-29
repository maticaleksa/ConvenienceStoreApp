package com.aleksa.data.source

import com.aleksa.data.database.SupplierDao
import com.aleksa.data.database.SupplierEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomSupplierDataSource @Inject constructor(
    private val supplierDao: SupplierDao,
) : SupplierDataSource {
    override fun getAllFlow(): Flow<List<SupplierEntity>> = supplierDao.getAllFlow()

    override fun searchFlow(query: String): Flow<List<SupplierEntity>> =
        supplierDao.searchFlow(query)

    override suspend fun getAll(): List<SupplierEntity> = supplierDao.getAll()

    override suspend fun getAllIds(): List<String> = supplierDao.getAllIds()

    override suspend fun upsertAll(suppliers: List<SupplierEntity>) =
        run {
            supplierDao.insertAll(suppliers)
            supplierDao.updateAll(suppliers)
        }

    override suspend fun upsert(supplier: SupplierEntity) {
        supplierDao.insert(supplier)
        supplierDao.update(supplier)
    }

    override suspend fun deleteByIds(ids: Collection<String>) = supplierDao.deleteByIds(ids)
}
