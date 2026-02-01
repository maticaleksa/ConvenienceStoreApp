package com.aleksa.data.source

import com.aleksa.data.database.ProductDao
import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.ProductWithCategorySupplier
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomProductDataSource @Inject constructor(
    private val productDao: ProductDao,
) : ProductDataSource {
    override suspend fun getAll(): List<ProductWithCategorySupplier> = productDao.getAll()

    override fun getAllFlow(): Flow<List<ProductWithCategorySupplier>> = productDao.getAllFlow()

    /**
     * Delegates to the DAO search; expects a wildcarded, lowercased LIKE pattern.
     */
    override fun searchFlow(query: String): Flow<List<ProductWithCategorySupplier>> =
        productDao.searchFlow(query)

    override suspend fun count(): Int = productDao.count()

    override suspend fun getAllIds(): List<String> = productDao.getAllIds()

    /**
     * Returns supplier ids that are referenced by at least one product.
     */
    override suspend fun getSupplierIdsInUse(): List<String> =
        productDao.getSupplierIdsInUse()

    override suspend fun getById(id: String): ProductWithCategorySupplier? = productDao.getById(id)

    override fun getByIdFlow(id: String): Flow<ProductWithCategorySupplier?> =
        productDao.getByIdFlow(id)

    override suspend fun upsertAll(products: List<ProductEntity>) = productDao.upsertAll(products)

    override suspend fun clearAll() = productDao.clearAll()

    override suspend fun upsert(product: ProductEntity) = productDao.upsert(product)

    override suspend fun upsertAndGet(product: ProductEntity): ProductWithCategorySupplier? {
        productDao.upsert(product)
        return productDao.getById(product.id)
    }

    override suspend fun delete(product: ProductEntity) = productDao.delete(product)

    override suspend fun deleteByIds(ids: Collection<String>) = productDao.deleteByIds(ids)
}
