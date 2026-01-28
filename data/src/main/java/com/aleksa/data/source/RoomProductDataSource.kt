package com.aleksa.data.source

import com.aleksa.data.database.ProductDao
import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.ProductWithCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomProductDataSource @Inject constructor(
    private val productDao: ProductDao,
) : ProductDataSource {
    override suspend fun getAll(): List<ProductWithCategory> = productDao.getAll()

    override fun getAllFlow(): Flow<List<ProductWithCategory>> = productDao.getAllFlow()

    override fun searchFlow(query: String): Flow<List<ProductWithCategory>> =
        productDao.searchFlow(query)

    override suspend fun count(): Int = productDao.count()

    override suspend fun getAllIds(): List<String> = productDao.getAllIds()

    override suspend fun getById(id: String): ProductWithCategory? = productDao.getById(id)

    override fun getByIdFlow(id: String): Flow<ProductWithCategory?> = productDao.getByIdFlow(id)

    override suspend fun upsertAll(products: List<ProductEntity>) = productDao.upsertAll(products)

    override suspend fun clearAll() = productDao.clearAll()

    override suspend fun upsert(product: ProductEntity) = productDao.upsert(product)

    override suspend fun delete(product: ProductEntity) = productDao.delete(product)

    override suspend fun deleteByIds(ids: Collection<String>) = productDao.deleteByIds(ids)
}
