package com.aleksa.data.repository

import com.aleksa.core.arch.coroutines.AppScope
import com.aleksa.data.database.toDomain
import com.aleksa.data.database.toEntity
import com.aleksa.data.database.toCategoryEntity
import com.aleksa.data.database.normalizeCategoryId
import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.toEntity as categoryToEntity
import com.aleksa.data.database.toEntity as supplierToEntity
import com.aleksa.data.source.ProductDataSource
import com.aleksa.data.source.CategoryDataSource
import com.aleksa.data.source.ProductRemoteDataSource
import com.aleksa.data.source.SupplierDataSource
import com.aleksa.data.source.SupplierRemoteDataSource
import com.aleksa.data.remote.toDto
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.error.ProductSyncError
import com.aleksa.domain.event.ProductDataCommand
import com.aleksa.domain.event.ProductDataCommand.RefreshAll
import com.aleksa.domain.model.Product
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val localDataSource: ProductDataSource,
    private val categoryDataSource: CategoryDataSource,
    private val supplierDataSource: SupplierDataSource,
    private val remoteDataSource: ProductRemoteDataSource,
    private val supplierRemoteDataSource: SupplierRemoteDataSource,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator,
    @AppScope private val coroutineScope: CoroutineScope,
) : ProductRepository {
    private companion object {
        private const val TAG = "ProductRepository"
    }
    val syncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)

    init {
        coroutineScope.launch {
            dataCommandBus.events
                .filterIsInstance<ProductDataCommand>()
                .collectLatest { event ->
                    when (event) {
                        RefreshAll -> refreshAllProducts()
                    }
                }
        }
        coroutineScope.launch {
            refreshAllProducts()
        }

    }

    override fun observeAll(): Flow<List<Product>> {
        return localDataSource.getAllFlow().map { productList ->
            productList.map { product ->
                product.toDomain()
            }
        }
    }

    override fun observeSearch(query: String): Flow<List<Product>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return observeAll()
        val likeQuery = "%${trimmed.lowercase()}%"
        return localDataSource.searchFlow(likeQuery).map { productList ->
            productList.map { product ->
                product.toDomain()
            }
        }
    }

    override suspend fun upsert(product: Product) {
        val safeCategoryId = product.category.id.ifBlank {
            normalizeCategoryId(product.category.name)
        }
        val safeCategory = product.category.copy(id = safeCategoryId)
        Log.d(
            TAG,
            "upsert: productId=${product.id} " +
                "categoryId=${safeCategory.id} categoryName=${safeCategory.name} " +
                "supplierId=${product.supplier.id}"
        )
        try {
            categoryDataSource.upsertAll(listOf(safeCategory.categoryToEntity()))
        } catch (e: Exception) {
            Log.e(TAG, "upsert: category upsert failed categoryId=${safeCategory.id}", e)
            throw e
        }
        try {
            supplierDataSource.upsert(product.supplier.supplierToEntity())
        } catch (e: Exception) {
            Log.e(TAG, "upsert: supplier upsert failed supplierId=${product.supplier.id}", e)
            throw e
        }
        val remoteResult = remoteDataSource.upsert(product.copy(category = safeCategory).toDto())
        when (remoteResult) {
            is NetworkResult.Success -> {
                val dto = remoteResult.data
                Log.d(TAG, "upsert(remote): dtoId=${dto.id} dtoCategory=${dto.category}")
                try {
                    categoryDataSource.upsertAll(listOf(dto.toCategoryEntity()))
                } catch (e: Exception) {
                    Log.e(TAG, "upsert(remote): category upsert failed dtoCategory=${dto.category}", e)
                    throw e
                }
                val normalizedDtoCategoryId = normalizeCategoryId(dto.category)
                try {
                    localDataSource.upsert(dto.toEntity(normalizedDtoCategoryId))
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "upsert(remote): product upsert failed dtoCategoryId=$normalizedDtoCategoryId",
                        e
                    )
                    throw e
                }
            }
            is NetworkResult.Error -> {
                Log.d(TAG, "upsert(remote): error=${remoteResult.error.message}")
                try {
                    localDataSource.upsert(product.copy(category = safeCategory).toEntity())
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "upsert(local): product upsert failed categoryId=${safeCategory.id}",
                        e
                    )
                    throw e
                }
            }
        }
    }

    private suspend fun refreshAllProducts() {
        syncChannel.execute {
            var hasSuppliers = false
            when (val suppliersResult = supplierRemoteDataSource.fetchAll()) {
                is NetworkResult.Success -> {
                    supplierDataSource.upsertAll(suppliersResult.data.map { it.toEntity() })
                    hasSuppliers = suppliersResult.data.isNotEmpty()
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "refresh: suppliers fetch failed ${suppliersResult.error.message}")
                    hasSuppliers = supplierDataSource.getAllIds().isNotEmpty()
                }
            }
            val networkResult = remoteDataSource.fetchAll()

            when (networkResult) {
                is NetworkResult.Success -> {
                    if (!hasSuppliers) {
                        Log.d(TAG, "refresh: skipped products sync due to empty suppliers")
                        return@execute
                    }
                    val categories = networkResult.data
                        .map { it.toCategoryEntity() }
                        .distinctBy { it.id }
                    categoryDataSource.upsertAll(categories)
                    val fresh = networkResult.data.map {
                        it.toEntity(normalizeCategoryId(it.category))
                    }
                    localDataSource.upsertAll(fresh)
                    removeProductsThatNoLongerExistOnRemote(fresh)
                }

                is NetworkResult.Error -> {
                    syncChannel.reportError(
                        error = ProductSyncError.Network(
                            message = networkResult.error.message,
                            code = networkResult.error.code,
                            details = networkResult.error.details
                        )
                    )
                    return@execute
                }

            }
        }
    }

    private suspend fun removeProductsThatNoLongerExistOnRemote(
        freshProducts: List<ProductEntity>,
    ) {
        val serverIds = freshProducts.map { it.id }.toSet()
        val localIds = localDataSource.getAllIds().toSet()
        val deletedIds = localIds - serverIds
        if (deletedIds.isNotEmpty()) {
            localDataSource.deleteByIds(deletedIds)
        }
    }
}
